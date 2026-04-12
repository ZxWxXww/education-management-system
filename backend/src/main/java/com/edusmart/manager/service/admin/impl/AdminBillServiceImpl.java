package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminBillPageItemDTO;
import com.edusmart.manager.dto.admin.AdminBillPaymentItemDTO;
import com.edusmart.manager.dto.admin.AdminBillPaymentSaveDTO;
import com.edusmart.manager.dto.admin.AdminBillRefundSaveDTO;
import com.edusmart.manager.dto.admin.AdminBillStudentOptionDTO;
import com.edusmart.manager.dto.admin.BillPageQueryDTO;
import com.edusmart.manager.dto.admin.BillSaveDTO;
import com.edusmart.manager.entity.EduBillEntity;
import com.edusmart.manager.entity.EduBillPaymentEntity;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduOrderEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduBillMapper;
import com.edusmart.manager.mapper.EduBillPaymentMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduOrderMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.HourPackageLedgerService;
import com.edusmart.manager.service.admin.AdminBillService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminBillServiceImpl implements AdminBillService {
    private final EduBillMapper billMapper;
    private final EduUserMapper userMapper;
    private final EduClassMapper classMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduBillPaymentMapper billPaymentMapper;
    private final EduOrderMapper orderMapper;
    private final CurrentUserService currentUserService;
    private final HourPackageLedgerService hourPackageLedgerService;

    public AdminBillServiceImpl(
            EduBillMapper billMapper,
            EduUserMapper userMapper,
            EduClassMapper classMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduBillPaymentMapper billPaymentMapper,
            EduOrderMapper orderMapper,
            CurrentUserService currentUserService,
            HourPackageLedgerService hourPackageLedgerService
    ) {
        this.billMapper = billMapper;
        this.userMapper = userMapper;
        this.classMapper = classMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.billPaymentMapper = billPaymentMapper;
        this.orderMapper = orderMapper;
        this.currentUserService = currentUserService;
        this.hourPackageLedgerService = hourPackageLedgerService;
    }

    @Override
    public PageData<AdminBillPageItemDTO> page(BillPageQueryDTO queryDTO) {
        QueryWrapper<EduBillEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like("bill_no", queryDTO.getKeyword()).or().like("remark", queryDTO.getKeyword()));
        }
        if (queryDTO.getStudentId() != null) {
            wrapper.eq("student_id", queryDTO.getStudentId());
        }
        if (StringUtils.hasText(queryDTO.getBillType())) {
            wrapper.eq("bill_type", queryDTO.getBillType());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getMonth())) {
            wrapper.apply("DATE_FORMAT(created_at, '%Y-%m') = {0}", queryDTO.getMonth());
        }
        wrapper.orderByDesc("id");
        Page<EduBillEntity> page = billMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildBillItems(page.getRecords()));
    }

    @Override
    public AdminBillPageItemDTO getById(Long id) {
        EduBillEntity bill = billMapper.selectById(id);
        if (bill == null) {
            return null;
        }
        return buildBillItems(List.of(bill)).stream().findFirst().orElse(null);
    }

    @Override
    public List<AdminBillStudentOptionDTO> listStudentOptions() {
        List<EduStudentProfileEntity> profiles = studentProfileMapper.selectList(new QueryWrapper<EduStudentProfileEntity>().orderByDesc("id"));
        if (profiles.isEmpty()) {
            return List.of();
        }

        Map<Long, EduUserEntity> userMap = userMapper.selectBatchIds(
                profiles.stream().map(EduStudentProfileEntity::getUserId).distinct().toList()
        ).stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item));

        List<AdminBillStudentOptionDTO> result = new ArrayList<>();
        for (EduStudentProfileEntity profile : profiles) {
            EduUserEntity user = userMap.get(profile.getUserId());
            AdminBillStudentOptionDTO item = new AdminBillStudentOptionDTO();
            item.setId(profile.getId());
            item.setUserId(profile.getUserId());
            item.setStudentNo(profile.getStudentNo());
            item.setStudentName(user == null ? "-" : (StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()));
            item.setClassNameText(profile.getClassNameText());
            result.add(item);
        }
        return result;
    }

    @Override
    public Long create(BillSaveDTO dto) {
        EduBillEntity entity = new EduBillEntity();
        entity.setBillNo(StringUtils.hasText(dto.getBillNo()) ? dto.getBillNo() : buildBillNo());
        entity.setOrderId(dto.getOrderId());
        entity.setStudentId(dto.getStudentId());
        entity.setClassId(dto.getClassId());
        entity.setBillType(dto.getBillType());
        entity.setAmount(dto.getAmount());
        entity.setPaidAmount(dto.getPaidAmount() == null ? BigDecimal.ZERO : dto.getPaidAmount());
        entity.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "PENDING");
        entity.setDueDate(dto.getDueDate());
        entity.setRemark(dto.getRemark());
        billMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, BillSaveDTO dto) {
        EduBillEntity old = billMapper.selectById(id);
        if (old == null) {
            return;
        }
        old.setBillNo(StringUtils.hasText(dto.getBillNo()) ? dto.getBillNo() : old.getBillNo());
        old.setOrderId(dto.getOrderId());
        old.setStudentId(dto.getStudentId());
        old.setClassId(dto.getClassId());
        old.setBillType(dto.getBillType());
        old.setAmount(dto.getAmount());
        old.setPaidAmount(dto.getPaidAmount() == null ? old.getPaidAmount() : dto.getPaidAmount());
        old.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : old.getStatus());
        old.setDueDate(dto.getDueDate());
        old.setRemark(dto.getRemark());
        billMapper.updateById(old);
    }

    @Override
    public void delete(Long id) {
        billMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Long registerPayment(Long id, AdminBillPaymentSaveDTO dto) {
        EduBillEntity bill = billMapper.selectById(id);
        if (bill == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "账单不存在");
        }
        if ("VOID".equals(bill.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已作废账单不能登记支付");
        }

        BigDecimal currentPaid = defaultAmount(bill.getPaidAmount());
        BigDecimal totalAmount = defaultAmount(bill.getAmount());
        BigDecimal remaining = totalAmount.subtract(currentPaid);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "账单已结清，无需重复登记支付");
        }
        if (dto.getPayAmount().compareTo(remaining) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "支付金额不能超过待收金额");
        }

        EduBillPaymentEntity payment = new EduBillPaymentEntity();
        payment.setBillId(id);
        payment.setPayNo(buildPaymentNo());
        payment.setPayAmount(dto.getPayAmount());
        payment.setPayChannel(normalizePayChannel(dto.getPayChannel()));
        payment.setPayTime(dto.getPayTime() == null ? LocalDateTime.now() : dto.getPayTime());
        payment.setOperatorUserId(currentUserService.getCurrentUserId());
        billPaymentMapper.insert(payment);

        bill.setPaidAmount(currentPaid.add(dto.getPayAmount()));
        bill.setStatus(resolveBillStatus(bill));
        billMapper.updateById(bill);

        syncOrderPaymentStatus(bill.getOrderId());
        if ("COMPLETED".equals(bill.getStatus())) {
            hourPackageLedgerService.reconcilePackagesForOrder(
                    bill.getOrderId(),
                    payment.getPayTime() == null ? LocalDate.now() : payment.getPayTime().toLocalDate()
            );
        }
        return payment.getId();
    }

    @Override
    @Transactional
    public Long registerRefund(Long id, AdminBillRefundSaveDTO dto) {
        EduBillEntity bill = billMapper.selectById(id);
        if (bill == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "账单不存在");
        }

        BigDecimal currentPaid = defaultAmount(bill.getPaidAmount());
        if (currentPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "账单当前无可退款金额");
        }
        if (dto.getRefundAmount().compareTo(currentPaid) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "退款金额不能超过已收金额");
        }

        LocalDateTime refundTime = dto.getPayTime() == null ? LocalDateTime.now() : dto.getPayTime();
        EduBillPaymentEntity refund = new EduBillPaymentEntity();
        refund.setBillId(id);
        refund.setPayNo(buildRefundNo(dto.getActionType()));
        refund.setPayAmount(dto.getRefundAmount().negate());
        refund.setPayChannel(normalizePayChannel(dto.getPayChannel()));
        refund.setPayTime(refundTime);
        refund.setOperatorUserId(currentUserService.getCurrentUserId());
        billPaymentMapper.insert(refund);

        bill.setPaidAmount(currentPaid.subtract(dto.getRefundAmount()).max(BigDecimal.ZERO));
        bill.setStatus(resolveRefundBillStatus(bill, dto.getActionType()));
        billMapper.updateById(bill);

        syncOrderPaymentStatus(bill.getOrderId(), dto.getActionType());
        hourPackageLedgerService.reconcilePackagesForOrder(
                bill.getOrderId(),
                refundTime.toLocalDate()
        );
        return refund.getId();
    }

    @Override
    public List<AdminBillPaymentItemDTO> listPayments(Long id) {
        List<EduBillPaymentEntity> payments = billPaymentMapper.selectList(
                new QueryWrapper<EduBillPaymentEntity>().eq("bill_id", id).orderByDesc("pay_time").orderByDesc("id")
        );
        if (payments.isEmpty()) {
            return List.of();
        }

        Map<Long, EduUserEntity> operatorMap = userMapper.selectBatchIds(
                payments.stream()
                        .map(EduBillPaymentEntity::getOperatorUserId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        ).stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> left));

        List<AdminBillPaymentItemDTO> result = new ArrayList<>();
        for (EduBillPaymentEntity payment : payments) {
            AdminBillPaymentItemDTO item = new AdminBillPaymentItemDTO();
            item.setId(payment.getId());
            item.setPayNo(payment.getPayNo());
            item.setPayAmount(payment.getPayAmount());
            item.setPayChannel(payment.getPayChannel());
            item.setPayTime(payment.getPayTime());
            item.setOperatorUserId(payment.getOperatorUserId());
            item.setCreatedAt(payment.getCreatedAt());
            EduUserEntity operator = payment.getOperatorUserId() == null ? null : operatorMap.get(payment.getOperatorUserId());
            item.setOperatorName(operator == null ? "-" : (StringUtils.hasText(operator.getRealName()) ? operator.getRealName() : operator.getUsername()));
            result.add(item);
        }
        return result;
    }

    private String buildBillNo() {
        return "BL" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + System.currentTimeMillis() % 100000;
    }

    private String buildPaymentNo() {
        return "PY" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + System.currentTimeMillis() % 100000;
    }

    private String buildRefundNo(String actionType) {
        String prefix = switch (normalizeActionType(actionType)) {
            case "REFUND" -> "RF";
            case "REVERSAL" -> "RV";
            case "UNSETTLE" -> "RU";
            default -> "RF";
        };
        return prefix + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + System.currentTimeMillis() % 100000;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String resolveBillStatus(EduBillEntity bill) {
        BigDecimal amount = defaultAmount(bill.getAmount());
        BigDecimal paidAmount = defaultAmount(bill.getPaidAmount());
        if (paidAmount.compareTo(amount) >= 0) {
            return "COMPLETED";
        }
        if (bill.getDueDate() != null && bill.getDueDate().isBefore(LocalDate.now())) {
            return "OVERDUE";
        }
        return "PENDING";
    }

    private String resolveRefundBillStatus(EduBillEntity bill, String actionType) {
        if (defaultAmount(bill.getPaidAmount()).compareTo(BigDecimal.ZERO) <= 0 && "REFUND".equals(normalizeActionType(actionType))) {
            return "VOID";
        }
        return resolveBillStatus(bill);
    }

    private void syncOrderPaymentStatus(Long orderId) {
        syncOrderPaymentStatus(orderId, null);
    }

    private void syncOrderPaymentStatus(Long orderId, String actionType) {
        if (orderId == null) {
            return;
        }
        EduOrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }

        BigDecimal paidAmount = billMapper.selectList(new QueryWrapper<EduBillEntity>().eq("order_id", orderId)).stream()
                .map(EduBillEntity::getPaidAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setAmountPaid(paidAmount);
        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            order.setPayStatus("REFUND".equals(normalizeActionType(actionType)) ? "REFUNDED" : "UNPAID");
        } else if (paidAmount.compareTo(defaultAmount(order.getAmountTotal())) >= 0) {
            order.setPayStatus("PAID");
        } else {
            order.setPayStatus("PARTIAL");
        }
        orderMapper.updateById(order);
    }

    private String normalizeActionType(String actionType) {
        return actionType == null ? "" : actionType.trim().toUpperCase();
    }

    private String normalizePayChannel(String payChannel) {
        if (!StringUtils.hasText(payChannel)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "支付渠道不能为空");
        }
        return switch (payChannel.trim().toUpperCase()) {
            case "CASH", "现金" -> "CASH";
            case "ALIPAY", "支付宝" -> "ALIPAY";
            case "WECHAT", "微信" -> "WECHAT";
            case "BANK", "BANK_TRANSFER", "银行转账" -> "BANK_TRANSFER";
            case "OTHER", "其他" -> "OTHER";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "支付渠道不合法");
        };
    }

    private List<AdminBillPageItemDTO> buildBillItems(List<EduBillEntity> bills) {
        if (bills == null || bills.isEmpty()) {
            return List.of();
        }

        List<Long> studentProfileIds = bills.stream()
                .map(EduBillEntity::getStudentId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        List<Long> classIds = bills.stream()
                .map(EduBillEntity::getClassId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, EduClassEntity> classMap = new HashMap<>();
        if (!classIds.isEmpty()) {
            classMap = classMapper.selectBatchIds(classIds).stream()
                    .collect(Collectors.toMap(EduClassEntity::getId, item -> item));
        }

        Map<Long, EduStudentProfileEntity> studentProfileMap = new HashMap<>();
        if (!studentProfileIds.isEmpty()) {
            studentProfileMap = studentProfileMapper.selectBatchIds(studentProfileIds)
                    .stream()
                    .collect(Collectors.toMap(EduStudentProfileEntity::getId, item -> item, (left, right) -> left));
        }

        Map<Long, EduUserEntity> userMap = new HashMap<>();
        if (!studentProfileMap.isEmpty()) {
            userMap = userMapper.selectBatchIds(
                    studentProfileMap.values().stream().map(EduStudentProfileEntity::getUserId).distinct().toList()
            ).stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item));
        }

        List<AdminBillPageItemDTO> result = new ArrayList<>();
        for (EduBillEntity bill : bills) {
            AdminBillPageItemDTO item = new AdminBillPageItemDTO();
            item.setId(bill.getId());
            item.setOrderId(bill.getOrderId());
            item.setBillNo(bill.getBillNo());
            item.setStudentId(bill.getStudentId());
            item.setClassId(bill.getClassId());
            item.setBillType(bill.getBillType());
            item.setAmount(bill.getAmount());
            item.setPaidAmount(bill.getPaidAmount());
            item.setStatus(bill.getStatus());
            item.setDueDate(bill.getDueDate());
            item.setRemark(bill.getRemark());
            item.setCreatedAt(bill.getCreatedAt());
            item.setUpdatedAt(bill.getUpdatedAt());

            EduStudentProfileEntity profile = studentProfileMap.get(bill.getStudentId());
            EduUserEntity student = profile == null ? null : userMap.get(profile.getUserId());
            item.setStudentName(student == null ? "-" : (StringUtils.hasText(student.getRealName()) ? student.getRealName() : student.getUsername()));

            EduClassEntity currentClass = classMap.get(bill.getClassId());
            if (currentClass != null) {
                item.setClassName(currentClass.getClassName());
            } else {
                item.setClassName(profile == null ? "-" : profile.getClassNameText());
            }

            result.add(item);
        }

        return result;
    }
}
