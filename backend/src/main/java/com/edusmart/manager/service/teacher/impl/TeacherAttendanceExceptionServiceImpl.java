package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionSaveDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.mapper.EduAttendanceExceptionMapper;
import com.edusmart.manager.service.teacher.TeacherAttendanceExceptionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TeacherAttendanceExceptionServiceImpl implements TeacherAttendanceExceptionService {
    private final EduAttendanceExceptionMapper mapper;
    public TeacherAttendanceExceptionServiceImpl(EduAttendanceExceptionMapper mapper) { this.mapper = mapper; }

    @Override
    public PageData<EduAttendanceExceptionEntity> page(TeacherAttendanceExceptionPageQueryDTO queryDTO) {
        QueryWrapper<EduAttendanceExceptionEntity> w = new QueryWrapper<>();
        if (queryDTO.getAttendanceRecordId() != null) w.eq("attendance_record_id", queryDTO.getAttendanceRecordId());
        if (queryDTO.getIsResolved() != null) w.eq("is_resolved", queryDTO.getIsResolved());
        if (queryDTO.getExceptionType() != null && !queryDTO.getExceptionType().isBlank()) w.eq("exception_type", queryDTO.getExceptionType());
        w.orderByDesc("id");
        Page<EduAttendanceExceptionEntity> p = mapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduAttendanceExceptionEntity getById(Long id) { return mapper.selectById(id); }

    @Override
    public Long create(TeacherAttendanceExceptionSaveDTO dto) {
        EduAttendanceExceptionEntity e = new EduAttendanceExceptionEntity();
        fill(e, dto);
        mapper.insert(e);
        return e.getId();
    }

    @Override
    public void update(Long id, TeacherAttendanceExceptionSaveDTO dto) {
        EduAttendanceExceptionEntity e = mapper.selectById(id);
        if (e == null) return;
        fill(e, dto);
        mapper.updateById(e);
    }

    @Override
    public void delete(Long id) { mapper.deleteById(id); }

    private void fill(EduAttendanceExceptionEntity e, TeacherAttendanceExceptionSaveDTO dto) {
        e.setAttendanceRecordId(dto.getAttendanceRecordId());
        e.setExceptionType(dto.getExceptionType());
        e.setSeverity(dto.getSeverity());
        e.setIsResolved(dto.getIsResolved());
        e.setResolvedBy(dto.getResolvedBy());
        e.setResolveRemark(dto.getResolveRemark());
        if (dto.getIsResolved() != null && dto.getIsResolved() == 1) e.setResolvedAt(LocalDateTime.now());
    }
}
