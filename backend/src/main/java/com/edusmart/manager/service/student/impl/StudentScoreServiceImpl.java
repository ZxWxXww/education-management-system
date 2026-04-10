package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentExamScorePageQueryDTO;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.mapper.EduExamScoreMapper;
import com.edusmart.manager.service.student.StudentScoreService;
import org.springframework.stereotype.Service;

@Service
public class StudentScoreServiceImpl implements StudentScoreService {
    private final EduExamScoreMapper scoreMapper;
    public StudentScoreServiceImpl(EduExamScoreMapper scoreMapper) { this.scoreMapper = scoreMapper; }

    @Override
    public PageData<EduExamScoreEntity> pageScores(StudentExamScorePageQueryDTO queryDTO) {
        QueryWrapper<EduExamScoreEntity> w = new QueryWrapper<>();
        if (queryDTO.getStudentId() != null) w.eq("student_id", queryDTO.getStudentId());
        if (queryDTO.getExamId() != null) w.eq("exam_id", queryDTO.getExamId());
        w.orderByDesc("id");
        Page<EduExamScoreEntity> p = scoreMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduExamScoreEntity getScore(Long id) {
        return scoreMapper.selectById(id);
    }
}
