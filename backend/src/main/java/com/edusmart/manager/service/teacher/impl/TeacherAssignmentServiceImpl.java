package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAssignmentPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAssignmentSaveDTO;
import com.edusmart.manager.entity.EduAssignmentEntity;
import com.edusmart.manager.mapper.EduAssignmentMapper;
import com.edusmart.manager.service.teacher.TeacherAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {
    private final EduAssignmentMapper assignmentMapper;
    public TeacherAssignmentServiceImpl(EduAssignmentMapper assignmentMapper) { this.assignmentMapper = assignmentMapper; }

    @Override
    public PageData<EduAssignmentEntity> page(TeacherAssignmentPageQueryDTO queryDTO) {
        QueryWrapper<EduAssignmentEntity> w = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) w.like("title", queryDTO.getKeyword());
        if (queryDTO.getClassId() != null) w.eq("class_id", queryDTO.getClassId());
        if (queryDTO.getCourseId() != null) w.eq("course_id", queryDTO.getCourseId());
        if (StringUtils.hasText(queryDTO.getStatus())) w.eq("status", queryDTO.getStatus());
        w.orderByDesc("id");
        Page<EduAssignmentEntity> p = assignmentMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduAssignmentEntity getById(Long id) { return assignmentMapper.selectById(id); }

    @Override
    public Long create(TeacherAssignmentSaveDTO dto) {
        EduAssignmentEntity e = new EduAssignmentEntity();
        fill(e, dto);
        assignmentMapper.insert(e);
        return e.getId();
    }

    @Override
    public void update(Long id, TeacherAssignmentSaveDTO dto) {
        EduAssignmentEntity e = assignmentMapper.selectById(id);
        if (e == null) return;
        fill(e, dto);
        assignmentMapper.updateById(e);
    }

    @Override
    public void delete(Long id) { assignmentMapper.deleteById(id); }

    private void fill(EduAssignmentEntity e, TeacherAssignmentSaveDTO dto) {
        e.setClassId(dto.getClassId());
        e.setCourseId(dto.getCourseId());
        e.setTeacherId(dto.getTeacherId());
        e.setTitle(dto.getTitle());
        e.setContent(dto.getContent());
        e.setAttachmentType(dto.getAttachmentType());
        e.setAttachmentUrl(dto.getAttachmentUrl());
        e.setPublishTime(dto.getPublishTime());
        e.setDeadline(dto.getDeadline());
        e.setStatus(dto.getStatus());
    }
}
