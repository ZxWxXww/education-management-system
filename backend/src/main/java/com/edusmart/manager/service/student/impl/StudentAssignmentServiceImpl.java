package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionSaveDTO;
import com.edusmart.manager.entity.EduAssignmentSubmissionEntity;
import com.edusmart.manager.mapper.EduAssignmentSubmissionMapper;
import com.edusmart.manager.service.student.StudentAssignmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StudentAssignmentServiceImpl implements StudentAssignmentService {
    private final EduAssignmentSubmissionMapper submissionMapper;

    public StudentAssignmentServiceImpl(EduAssignmentSubmissionMapper submissionMapper) {
        this.submissionMapper = submissionMapper;
    }

    @Override
    public PageData<EduAssignmentSubmissionEntity> pageSubmissions(StudentAssignmentSubmissionPageQueryDTO queryDTO) {
        QueryWrapper<EduAssignmentSubmissionEntity> w = new QueryWrapper<>();
        if (queryDTO.getStudentId() != null) w.eq("student_id", queryDTO.getStudentId());
        if (queryDTO.getAssignmentId() != null) w.eq("assignment_id", queryDTO.getAssignmentId());
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isBlank()) w.eq("status", queryDTO.getStatus());
        w.orderByDesc("id");
        Page<EduAssignmentSubmissionEntity> p = submissionMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduAssignmentSubmissionEntity getSubmission(Long id) {
        return submissionMapper.selectById(id);
    }

    @Override
    public Long createSubmission(StudentAssignmentSubmissionSaveDTO dto) {
        EduAssignmentSubmissionEntity e = new EduAssignmentSubmissionEntity();
        fill(e, dto);
        if (e.getSubmitTime() == null) e.setSubmitTime(LocalDateTime.now());
        submissionMapper.insert(e);
        return e.getId();
    }

    @Override
    public void updateSubmission(Long id, StudentAssignmentSubmissionSaveDTO dto) {
        EduAssignmentSubmissionEntity e = submissionMapper.selectById(id);
        if (e == null) return;
        fill(e, dto);
        if (e.getSubmitTime() == null) e.setSubmitTime(LocalDateTime.now());
        submissionMapper.updateById(e);
    }

    @Override
    public void deleteSubmission(Long id) {
        submissionMapper.deleteById(id);
    }

    private void fill(EduAssignmentSubmissionEntity e, StudentAssignmentSubmissionSaveDTO dto) {
        e.setAssignmentId(dto.getAssignmentId());
        e.setStudentId(dto.getStudentId());
        e.setSubmitTime(dto.getSubmitTime());
        e.setSubmitContent(dto.getSubmitContent());
        e.setAttachmentType(dto.getAttachmentType());
        e.setAttachmentUrl(dto.getAttachmentUrl());
        e.setStatus(dto.getStatus());
    }
}
