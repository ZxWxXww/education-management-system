package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAttendancePageQueryDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.mapper.EduAttendanceExceptionMapper;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.service.student.StudentAttendanceService;
import org.springframework.stereotype.Service;

@Service
public class StudentAttendanceServiceImpl implements StudentAttendanceService {
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduAttendanceExceptionMapper attendanceExceptionMapper;

    public StudentAttendanceServiceImpl(EduAttendanceRecordMapper attendanceRecordMapper, EduAttendanceExceptionMapper attendanceExceptionMapper) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.attendanceExceptionMapper = attendanceExceptionMapper;
    }

    @Override
    public PageData<EduAttendanceRecordEntity> pageAttendance(StudentAttendancePageQueryDTO queryDTO) {
        QueryWrapper<EduAttendanceRecordEntity> w = new QueryWrapper<>();
        if (queryDTO.getStudentId() != null) w.eq("student_id", queryDTO.getStudentId());
        if (queryDTO.getClassId() != null) w.eq("class_id", queryDTO.getClassId());
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isBlank()) w.eq("status", queryDTO.getStatus());
        if (queryDTO.getAttendanceDate() != null) w.eq("attendance_date", queryDTO.getAttendanceDate());
        w.orderByDesc("id");
        Page<EduAttendanceRecordEntity> p = attendanceRecordMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduAttendanceRecordEntity getAttendance(Long id) {
        return attendanceRecordMapper.selectById(id);
    }

    @Override
    public PageData<EduAttendanceExceptionEntity> pageExceptions(StudentAttendanceExceptionPageQueryDTO queryDTO) {
        QueryWrapper<EduAttendanceExceptionEntity> w = new QueryWrapper<>();
        if (queryDTO.getAttendanceRecordId() != null) w.eq("attendance_record_id", queryDTO.getAttendanceRecordId());
        if (queryDTO.getIsResolved() != null) w.eq("is_resolved", queryDTO.getIsResolved());
        if (queryDTO.getExceptionType() != null && !queryDTO.getExceptionType().isBlank()) w.eq("exception_type", queryDTO.getExceptionType());
        w.orderByDesc("id");
        Page<EduAttendanceExceptionEntity> p = attendanceExceptionMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduAttendanceExceptionEntity getException(Long id) {
        return attendanceExceptionMapper.selectById(id);
    }
}
