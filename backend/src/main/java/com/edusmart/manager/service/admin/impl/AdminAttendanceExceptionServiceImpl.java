package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.admin.AttendanceExceptionSaveDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.mapper.EduAttendanceExceptionMapper;
import com.edusmart.manager.service.admin.AdminAttendanceExceptionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminAttendanceExceptionServiceImpl implements AdminAttendanceExceptionService {
    private final EduAttendanceExceptionMapper attendanceExceptionMapper;

    public AdminAttendanceExceptionServiceImpl(EduAttendanceExceptionMapper attendanceExceptionMapper) {
        this.attendanceExceptionMapper = attendanceExceptionMapper;
    }

    @Override
    public PageData<EduAttendanceExceptionEntity> page(AttendanceExceptionPageQueryDTO queryDTO) {
        QueryWrapper<EduAttendanceExceptionEntity> wrapper = new QueryWrapper<>();
        if (queryDTO.getAttendanceRecordId() != null) {
            wrapper.eq("attendance_record_id", queryDTO.getAttendanceRecordId());
        }
        if (queryDTO.getIsResolved() != null) {
            wrapper.eq("is_resolved", queryDTO.getIsResolved());
        }
        if (queryDTO.getExceptionType() != null && !queryDTO.getExceptionType().isBlank()) {
            wrapper.eq("exception_type", queryDTO.getExceptionType());
        }
        wrapper.orderByDesc("id");
        Page<EduAttendanceExceptionEntity> page = attendanceExceptionMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public EduAttendanceExceptionEntity getById(Long id) {
        return attendanceExceptionMapper.selectById(id);
    }

    @Override
    public Long create(AttendanceExceptionSaveDTO dto) {
        EduAttendanceExceptionEntity entity = new EduAttendanceExceptionEntity();
        fill(entity, dto);
        attendanceExceptionMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, AttendanceExceptionSaveDTO dto) {
        EduAttendanceExceptionEntity old = attendanceExceptionMapper.selectById(id);
        if (old == null) {
            return;
        }
        fill(old, dto);
        attendanceExceptionMapper.updateById(old);
    }

    @Override
    public void delete(Long id) {
        attendanceExceptionMapper.deleteById(id);
    }

    private void fill(EduAttendanceExceptionEntity entity, AttendanceExceptionSaveDTO dto) {
        entity.setAttendanceRecordId(dto.getAttendanceRecordId());
        entity.setExceptionType(dto.getExceptionType());
        entity.setSeverity(dto.getSeverity());
        entity.setIsResolved(dto.getIsResolved() == null ? 0 : dto.getIsResolved());
        entity.setResolvedBy(dto.getResolvedBy());
        entity.setResolveRemark(dto.getResolveRemark());
        if (entity.getIsResolved() != null && entity.getIsResolved() == 1) {
            entity.setResolvedAt(LocalDateTime.now());
        } else {
            entity.setResolvedAt(null);
        }
    }
}
