package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminOrderDetailDTO;
import com.edusmart.manager.dto.admin.AdminOrderDetailItemDTO;
import com.edusmart.manager.dto.admin.AdminOrderItemSaveDTO;
import com.edusmart.manager.dto.admin.AdminOrderPageItemDTO;
import com.edusmart.manager.dto.admin.AdminOrderPageQueryDTO;
import com.edusmart.manager.dto.admin.AdminOrderSaveDTO;
import com.edusmart.manager.entity.EduBillEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduOrderEntity;
import com.edusmart.manager.entity.EduOrderItemEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduBillMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduOrderItemMapper;
import com.edusmart.manager.mapper.EduOrderMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.service.admin.AdminOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {
    private final EduOrderMapper orderMapper;
    private final EduOrderItemMapper orderItemMapper;
    private final EduBillMapper billMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduUserMapper userMapper;
    private final EduCourseMapper courseMapper;

    public AdminOrderServiceImpl(EduOrderMapper orderMapper,
                                 EduOrderItemMapper orderItemMapper,
                                 EduBillMapper billMapper,
                                 EduStudentProfileMapper studentProfileMapper,
                                 EduUserMapper userMapper,
                                 EduCourseMapper courseMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.billMapper = billMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    public PageData<AdminOrderPageItemDTO> page(AdminOrderPageQueryDTO queryDTO) {
        QueryWrapper<EduOrderEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like("order_no", queryDTO.getKeyword()).or().like("remark", queryDTO.getKeyword()));
        }
        if (queryDTO.getStudentId() != null) {
            wrapper.eq("student_id", queryDTO.getStudentId());
        }
        if (StringUtils.hasText(queryDTO.getOrderType())) {
            wrapper.eq("order_type", queryDTO.getOrderType());
        }
        if (StringUtils.hasText(queryDTO.getPayStatus())) {
            wrapper.eq("pay_status", queryDTO.getPayStatus());
        }
        wrapper.orderByDesc("id");
        Page<EduOrderEntity> page = orderMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildOrderPageItems(page.getRecords()));
    }

    @Override
    public AdminOrderDetailDTO getById(Long id) {
        EduOrderEntity order = orderMapper.selectById(id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }

        AdminOrderPageItemDTO summary = buildOrderPageItems(List.of(order)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));

        List<EduOrderItemEntity> orderItems = orderItemMapper.selectList(
                new QueryWrapper<EduOrderItemEntity>().eq("order_id", id).orderByAsc("id")
        );
        Map<Long, EduCourseEntity> courseMap = loadCourseMap(orderItems.stream()
                .map(EduOrderItemEntity::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        AdminOrderDetailDTO detail = new AdminOrderDetailDTO();
        copySummary(summary, detail);
        detail.setItems(orderItems.stream().map(item -> toDetailItem(item, courseMap)).toList());
        return detail;
    }

    @Override
    @Transactional
    public Long create(AdminOrderSaveDTO dto) {
        EduStudentProfileEntity studentProfile = studentProfileMapper.selectById(dto.getStudentId());
        if (studentProfile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "学员档案不存在");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单明细不能为空");
        }

        BigDecimal amountTotal = dto.getItems().stream()
                .map(this::calculateLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        EduOrderEntity order = new EduOrderEntity();
        order.setOrderNo(buildOrderNo());
        order.setStudentId(dto.getStudentId());
        order.setOrderType(StringUtils.hasText(dto.getOrderType()) ? dto.getOrderType() : "HOUR_PACKAGE");
        order.setAmountTotal(amountTotal);
        order.setAmountPaid(BigDecimal.ZERO);
        order.setPayStatus("UNPAID");
        order.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        orderMapper.insert(order);

        for (AdminOrderItemSaveDTO itemDTO : dto.getItems()) {
            EduOrderItemEntity item = new EduOrderItemEntity();
            item.setOrderId(order.getId());
            item.setCourseId(itemDTO.getCourseId());
            item.setItemName(itemDTO.getItemName().trim());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setLineAmount(calculateLineAmount(itemDTO));
            item.setHourCount(itemDTO.getHourCount());
            orderItemMapper.insert(item);
        }

        EduBillEntity bill = new EduBillEntity();
        bill.setBillNo(buildBillNo());
        bill.setOrderId(order.getId());
        bill.setStudentId(dto.getStudentId());
        bill.setBillType(order.getOrderType());
        bill.setAmount(amountTotal);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStatus("PENDING");
        bill.setDueDate(dto.getDueDate());
        bill.setRemark(order.getRemark());
        billMapper.insert(bill);
        return order.getId();
    }

    private BigDecimal calculateLineAmount(AdminOrderItemSaveDTO itemDTO) {
        return itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
    }

    private String buildOrderNo() {
        return "OD" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + System.currentTimeMillis() % 100000;
    }

    private String buildBillNo() {
        return "BL" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + System.currentTimeMillis() % 100000;
    }

    private List<AdminOrderPageItemDTO> buildOrderPageItems(List<EduOrderEntity> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        List<Long> studentProfileIds = orders.stream()
                .map(EduOrderEntity::getStudentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, EduStudentProfileEntity> studentProfileMap = studentProfileIds.isEmpty()
                ? Map.of()
                : studentProfileMapper.selectBatchIds(studentProfileIds).stream()
                .collect(Collectors.toMap(EduStudentProfileEntity::getId, Function.identity(), (left, right) -> left));

        List<Long> userIds = studentProfileMap.values().stream()
                .map(EduStudentProfileEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, EduUserEntity> userMap = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(EduUserEntity::getId, Function.identity(), (left, right) -> left));

        List<Long> orderIds = orders.stream().map(EduOrderEntity::getId).filter(Objects::nonNull).toList();
        Map<Long, Integer> itemCountMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            orderItemMapper.selectList(new QueryWrapper<EduOrderItemEntity>().in("order_id", orderIds))
                    .forEach(item -> itemCountMap.merge(item.getOrderId(), 1, Integer::sum));
        }

        Map<Long, EduBillEntity> billMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            billMapper.selectList(new QueryWrapper<EduBillEntity>().in("order_id", orderIds))
                    .forEach(bill -> billMap.putIfAbsent(bill.getOrderId(), bill));
        }

        List<AdminOrderPageItemDTO> result = new ArrayList<>();
        for (EduOrderEntity order : orders) {
            EduStudentProfileEntity profile = studentProfileMap.get(order.getStudentId());
            EduUserEntity user = profile == null ? null : userMap.get(profile.getUserId());
            EduBillEntity bill = billMap.get(order.getId());

            AdminOrderPageItemDTO item = new AdminOrderPageItemDTO();
            item.setId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setStudentId(order.getStudentId());
            item.setStudentName(resolveStudentName(profile, user));
            item.setOrderType(order.getOrderType());
            item.setAmountTotal(order.getAmountTotal());
            item.setAmountPaid(order.getAmountPaid());
            item.setPayStatus(order.getPayStatus());
            item.setItemCount(itemCountMap.getOrDefault(order.getId(), 0));
            item.setBillId(bill == null ? null : bill.getId());
            item.setBillNo(bill == null ? null : bill.getBillNo());
            item.setBillStatus(bill == null ? null : bill.getStatus());
            item.setDueDate(bill == null ? null : bill.getDueDate());
            item.setRemark(order.getRemark());
            item.setCreatedAt(order.getCreatedAt());
            item.setUpdatedAt(order.getUpdatedAt());
            result.add(item);
        }
        return result;
    }

    private Map<Long, EduCourseEntity> loadCourseMap(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }
        return courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(EduCourseEntity::getId, Function.identity(), (left, right) -> left));
    }

    private AdminOrderDetailItemDTO toDetailItem(EduOrderItemEntity entity, Map<Long, EduCourseEntity> courseMap) {
        AdminOrderDetailItemDTO dto = new AdminOrderDetailItemDTO();
        dto.setId(entity.getId());
        dto.setCourseId(entity.getCourseId());
        dto.setItemName(entity.getItemName());
        dto.setQuantity(entity.getQuantity());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setLineAmount(entity.getLineAmount());
        dto.setHourCount(entity.getHourCount());
        EduCourseEntity course = entity.getCourseId() == null ? null : courseMap.get(entity.getCourseId());
        dto.setCourseName(course == null ? null : course.getCourseName());
        return dto;
    }

    private String resolveStudentName(EduStudentProfileEntity profile, EduUserEntity user) {
        if (user != null && StringUtils.hasText(user.getRealName())) {
            return user.getRealName();
        }
        if (user != null && StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        if (profile != null && StringUtils.hasText(profile.getStudentNo())) {
            return profile.getStudentNo();
        }
        return "-";
    }

    private void copySummary(AdminOrderPageItemDTO source, AdminOrderDetailDTO target) {
        target.setId(source.getId());
        target.setOrderNo(source.getOrderNo());
        target.setStudentId(source.getStudentId());
        target.setStudentName(source.getStudentName());
        target.setOrderType(source.getOrderType());
        target.setAmountTotal(source.getAmountTotal());
        target.setAmountPaid(source.getAmountPaid());
        target.setPayStatus(source.getPayStatus());
        target.setItemCount(source.getItemCount());
        target.setBillId(source.getBillId());
        target.setBillNo(source.getBillNo());
        target.setBillStatus(source.getBillStatus());
        target.setDueDate(source.getDueDate());
        target.setRemark(source.getRemark());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }
}
