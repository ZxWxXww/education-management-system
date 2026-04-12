package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentExamScorePageQueryDTO;
import com.edusmart.manager.dto.student.StudentScorePageItemDTO;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduExamEntity;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduExamMapper;
import com.edusmart.manager.mapper.EduExamScoreMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.student.StudentScoreService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudentScoreServiceImpl implements StudentScoreService {
    private final EduExamScoreMapper scoreMapper;
    private final EduExamMapper examMapper;
    private final EduCourseMapper courseMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final CurrentUserService currentUserService;

    public StudentScoreServiceImpl(
            EduExamScoreMapper scoreMapper,
            EduExamMapper examMapper,
            EduCourseMapper courseMapper,
            EduStudentProfileMapper studentProfileMapper,
            CurrentUserService currentUserService
    ) {
        this.scoreMapper = scoreMapper;
        this.examMapper = examMapper;
        this.courseMapper = courseMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public PageData<StudentScorePageItemDTO> pageScores(StudentExamScorePageQueryDTO queryDTO) {
        Long studentProfileId = getCurrentStudentProfileId();
        QueryWrapper<EduExamScoreEntity> w = new QueryWrapper<EduExamScoreEntity>().eq("student_id", studentProfileId);
        if (queryDTO.getExamId() != null) w.eq("exam_id", queryDTO.getExamId());
        w.orderByDesc("id");
        Page<EduExamScoreEntity> p = scoreMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), buildItems(p.getRecords()));
    }

    @Override
    public StudentScorePageItemDTO getScore(Long id) {
        EduExamScoreEntity score = scoreMapper.selectById(id);
        Long studentProfileId = getCurrentStudentProfileId();
        if (score == null || !Objects.equals(score.getStudentId(), studentProfileId)) {
            throw new ResponseStatusException(NOT_FOUND, "成绩不存在");
        }
        return buildItems(Collections.singletonList(score)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "成绩不存在"));
    }

    private Long getCurrentStudentProfileId() {
        EduStudentProfileEntity profile = studentProfileMapper.selectOne(new QueryWrapper<EduStudentProfileEntity>()
                .eq("user_id", currentUserService.getCurrentUserId()));
        if (profile == null) {
            throw new ResponseStatusException(NOT_FOUND, "学生档案不存在");
        }
        return profile.getId();
    }

    private List<StudentScorePageItemDTO> buildItems(List<EduExamScoreEntity> scores) {
        if (scores == null || scores.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduExamEntity> examMap = examMapper.selectBatchIds(
                        scores.stream().map(EduExamScoreEntity::getExamId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduExamEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduCourseEntity> courseMap = courseMapper.selectBatchIds(
                        examMap.values().stream().map(EduExamEntity::getCourseId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> right));

        return scores.stream().map(score -> {
            EduExamEntity exam = examMap.get(score.getExamId());
            EduCourseEntity course = exam == null ? null : courseMap.get(exam.getCourseId());
            StudentScorePageItemDTO item = new StudentScorePageItemDTO();
            item.setId(score.getId());
            item.setExamId(score.getExamId());
            item.setExamName(exam == null ? "" : exam.getExamName());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setExamStatus(exam == null ? "" : exam.getStatus());
            item.setScore(score.getScore());
            item.setFullScore(exam == null ? BigDecimal.ZERO : exam.getFullScore());
            item.setRankInClass(score.getRankInClass());
            item.setTeacherComment(score.getTeacherComment());
            item.setPublishTime(score.getPublishTime());
            item.setExamTime(exam == null ? null : exam.getExamTime());
            item.setCreatedAt(score.getCreatedAt());
            item.setUpdatedAt(score.getUpdatedAt());
            return item;
        }).collect(Collectors.toList());
    }
}
