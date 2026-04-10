package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAttendancePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceSaveDTO;
import com.edusmart.manager.dto.teacher.TeacherBatchAttendanceSaveDTO;
import com.edusmart.manager.entity.EduAttendanceBatchTaskEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.mapper.EduAttendanceBatchTaskMapper;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.service.teacher.TeacherAttendanceService;
import org.springframework.stereotype.Service;

@Service
public class TeacherAttendanceServiceImpl implements TeacherAttendanceService {
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduAttendanceBatchTaskMapper batchTaskMapper;

    public TeacherAttendanceServiceImpl(EduAttendanceRecordMapper attendanceRecordMapper, EduAttendanceBatchTaskMapper batchTaskMapper) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.batchTaskMapper = batchTaskMapper;
    }

    @Override
    public PageData<EduAttendanceRecordEntity> page(TeacherAttendancePageQueryDTO queryDTO) {
        QueryWrapper<EduAttendanceRecordEntity> w = new QueryWrapper<>();
        if (queryDTO.getClassId() != null) w.eq("class_id", queryDTO.getClassId());
        if (queryDTO.getStudentId() != null) w.eq("student_id", queryDTO.getStudentId());
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isBlank()) w.eq("status", queryDTO.getStatus());
        if (queryDTO.getAttendanceDate() != null) w.eq("attendance_date", queryDTO.getAttendanceDate());
        w.orderByDesc("id");
        Page<EduAttendanceRecordEntity> p = attendanceRecordMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduAttendanceRecordEntity getById(Long id) { return attendanceRecordMapper.selectById(id); }

    @Override
    public Long create(TeacherAttendanceSaveDTO dto) {
        EduAttendanceRecordEntity e = new EduAttendanceRecordEntity();
        fill(e, dto);
        attendanceRecordMapper.insert(e);
        return e.getId();
    }

    @Override
    public void update(Long id, TeacherAttendanceSaveDTO dto) {
        EduAttendanceRecordEntity e = attendanceRecordMapper.selectById(id);
        if (e == null) return;
        fill(e, dto);
        attendanceRecordMapper.updateById(e);
    }

    @Override
    public void delete(Long id) { attendanceRecordMapper.deleteById(id); }

    @Override
    public Long createBatchTask(TeacherBatchAttendanceSaveDTO dto) {
        EduAttendanceBatchTaskEntity e = new EduAttendanceBatchTaskEntity();
        e.setOperatorTeacherId(dto.getOperatorTeacherId());
        e.setClassId(dto.getClassId());
        e.setAttendanceDate(dto.getAttendanceDate());
        e.setTaskStatus(dto.getTaskStatus());
        e.setRemark(dto.getRemark());
        batchTaskMapper.insert(e);
        return e.getId();
    }

    @Override
    public EduAttendanceBatchTaskEntity getBatchTask(Long id) { return batchTaskMapper.selectById(id); }

    private void fill(EduAttendanceRecordEntity e, TeacherAttendanceSaveDTO dto) {
        e.setClassId(dto.getClassId());
        e.setSessionId(dto.getSessionId());
        e.setStudentId(dto.getStudentId());
        e.setTeacherId(dto.getTeacherId());
        e.setAttendanceDate(dto.getAttendanceDate());
        e.setStatus(dto.getStatus());
        e.setRemark(dto.getRemark());
    }
}
