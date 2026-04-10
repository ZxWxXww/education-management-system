package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.BillPageQueryDTO;
import com.edusmart.manager.dto.admin.BillSaveDTO;
import com.edusmart.manager.entity.EduBillEntity;
import com.edusmart.manager.mapper.EduBillMapper;
import com.edusmart.manager.service.admin.AdminBillService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AdminBillServiceImpl implements AdminBillService {
    private final EduBillMapper billMapper;

    public AdminBillServiceImpl(EduBillMapper billMapper) {
        this.billMapper = billMapper;
    }

    @Override
    public PageData<EduBillEntity> page(BillPageQueryDTO queryDTO) {
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
        wrapper.orderByDesc("id");
        Page<EduBillEntity> page = billMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public EduBillEntity getById(Long id) {
        return billMapper.selectById(id);
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

    private String buildBillNo() {
        return "BL" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + System.currentTimeMillis() % 100000;
    }
}
