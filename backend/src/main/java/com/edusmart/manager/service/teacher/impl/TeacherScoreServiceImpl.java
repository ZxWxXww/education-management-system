package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherScorePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherScoreSaveDTO;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.mapper.EduExamScoreMapper;
import com.edusmart.manager.service.teacher.TeacherScoreService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeacherScoreServiceImpl implements TeacherScoreService {
    private final EduExamScoreMapper scoreMapper;
    public TeacherScoreServiceImpl(EduExamScoreMapper scoreMapper) { this.scoreMapper = scoreMapper; }

    @Override
    public PageData<EduExamScoreEntity> page(TeacherScorePageQueryDTO queryDTO) {
        QueryWrapper<EduExamScoreEntity> w = new QueryWrapper<>();
        if (queryDTO.getExamId() != null) w.eq("exam_id", queryDTO.getExamId());
        if (queryDTO.getStudentId() != null) w.eq("student_id", queryDTO.getStudentId());
        w.orderByDesc("id");
        Page<EduExamScoreEntity> p = scoreMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduExamScoreEntity getById(Long id) { return scoreMapper.selectById(id); }

    @Override
    public Long create(TeacherScoreSaveDTO dto) {
        EduExamScoreEntity e = new EduExamScoreEntity();
        fill(e, dto);
        scoreMapper.insert(e);
        return e.getId();
    }

    @Override
    public void update(Long id, TeacherScoreSaveDTO dto) {
        EduExamScoreEntity e = scoreMapper.selectById(id);
        if (e == null) return;
        fill(e, dto);
        scoreMapper.updateById(e);
    }

    @Override
    public void delete(Long id) { scoreMapper.deleteById(id); }

    @Override
    public Map<String, Object> analysis(Long examId) {
        QueryWrapper<EduExamScoreEntity> w = new QueryWrapper<>();
        w.eq("exam_id", examId);
        List<EduExamScoreEntity> rows = scoreMapper.selectList(w);
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = null;
        BigDecimal min = null;
        for (EduExamScoreEntity row : rows) {
            BigDecimal s = row.getScore();
            sum = sum.add(s);
            max = (max == null || s.compareTo(max) > 0) ? s : max;
            min = (min == null || s.compareTo(min) < 0) ? s : min;
        }
        BigDecimal avg = rows.isEmpty() ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(rows.size()), 2, java.math.RoundingMode.HALF_UP);
        Map<String, Object> map = new HashMap<>();
        map.put("examId", examId);
        map.put("studentCount", rows.size());
        map.put("avgScore", avg);
        map.put("maxScore", max == null ? BigDecimal.ZERO : max);
        map.put("minScore", min == null ? BigDecimal.ZERO : min);
        return map;
    }

    private void fill(EduExamScoreEntity e, TeacherScoreSaveDTO dto) {
        e.setExamId(dto.getExamId());
        e.setStudentId(dto.getStudentId());
        e.setScore(dto.getScore());
        e.setRankInClass(dto.getRankInClass());
        e.setTeacherComment(dto.getTeacherComment());
        e.setPublishTime(dto.getPublishTime());
    }
}
