package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherScorePageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherScorePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherScoreSaveDTO;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduExamEntity;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduExamMapper;
import com.edusmart.manager.mapper.EduExamScoreMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.teacher.TeacherScoreService;
import com.edusmart.manager.service.teacher.TeacherScopeGuard;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TeacherScoreServiceImpl implements TeacherScoreService {
    private final EduExamScoreMapper scoreMapper;
    private final EduExamMapper examMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final CurrentUserService currentUserService;
    private final TeacherScopeGuard teacherScopeGuard;

    public TeacherScoreServiceImpl(
            EduExamScoreMapper scoreMapper,
            EduExamMapper examMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            CurrentUserService currentUserService,
            TeacherScopeGuard teacherScopeGuard
    ) {
        this.scoreMapper = scoreMapper;
        this.examMapper = examMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
        this.teacherScopeGuard = teacherScopeGuard;
    }

    @Override
    public PageData<TeacherScorePageItemDTO> page(TeacherScorePageQueryDTO queryDTO) {
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (teacherProfile == null) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }
        List<Long> examIds = examMapper.selectList(new QueryWrapper<EduExamEntity>().eq("teacher_id", teacherProfile.getId()))
                .stream()
                .map(EduExamEntity::getId)
                .collect(Collectors.toList());
        if (examIds.isEmpty()) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }
        QueryWrapper<EduExamScoreEntity> w = new QueryWrapper<EduExamScoreEntity>().in("exam_id", examIds);
        if (queryDTO.getExamId() != null) w.eq("exam_id", queryDTO.getExamId());
        if (queryDTO.getStudentId() != null) w.eq("student_id", queryDTO.getStudentId());
        w.orderByDesc("id");
        Page<EduExamScoreEntity> p = scoreMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), buildItems(p.getRecords()));
    }

    @Override
    public TeacherScorePageItemDTO getById(Long id) {
        EduExamScoreEntity score = teacherScopeGuard.requireOwnedScore(id);
        return buildItems(Collections.singletonList(score)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "成绩不存在"));
    }

    @Override
    public Long create(TeacherScoreSaveDTO dto) {
        EduExamEntity exam = teacherScopeGuard.requireOwnedExam(dto.getExamId());
        teacherScopeGuard.requireStudentInClass(dto.getStudentId(), exam.getClassId());
        EduExamScoreEntity e = new EduExamScoreEntity();
        fill(e, dto);
        scoreMapper.insert(e);
        return e.getId();
    }

    @Override
    public void update(Long id, TeacherScoreSaveDTO dto) {
        EduExamScoreEntity e = teacherScopeGuard.requireOwnedScore(id);
        EduExamEntity exam = teacherScopeGuard.requireOwnedExam(dto.getExamId());
        teacherScopeGuard.requireStudentInClass(dto.getStudentId(), exam.getClassId());
        fill(e, dto);
        scoreMapper.updateById(e);
    }

    @Override
    public void delete(Long id) {
        teacherScopeGuard.requireOwnedScore(id);
        scoreMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> analysis(Long examId) {
        teacherScopeGuard.requireOwnedExam(examId);
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
        BigDecimal avg = rows.isEmpty() ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP);
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

    private EduTeacherProfileEntity getCurrentTeacherProfile() {
        return teacherProfileMapper.selectOne(new QueryWrapper<EduTeacherProfileEntity>()
                .eq("user_id", currentUserService.getCurrentUserId()));
    }

    private List<TeacherScorePageItemDTO> buildItems(List<EduExamScoreEntity> scores) {
        if (scores == null || scores.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> examIds = scores.stream().map(EduExamScoreEntity::getExamId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> studentIds = scores.stream().map(EduExamScoreEntity::getStudentId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, EduExamEntity> examMap = examMapper.selectBatchIds(examIds).stream()
                .collect(Collectors.toMap(EduExamEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduStudentProfileEntity> studentMap = studentProfileMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(EduStudentProfileEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(
                        examMap.values().stream().map(EduExamEntity::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduCourseEntity> courseMap = courseMapper.selectBatchIds(
                        examMap.values().stream().map(EduExamEntity::getCourseId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduUserEntity> studentUserMap = userMapper.selectBatchIds(
                        studentMap.values().stream().map(EduStudentProfileEntity::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right));

        return scores.stream().map(score -> {
            EduExamEntity exam = examMap.get(score.getExamId());
            EduStudentProfileEntity student = studentMap.get(score.getStudentId());
            EduUserEntity studentUser = student == null ? null : studentUserMap.get(student.getUserId());
            EduClassEntity clazz = exam == null ? null : classMap.get(exam.getClassId());
            EduCourseEntity course = exam == null ? null : courseMap.get(exam.getCourseId());
            TeacherScorePageItemDTO item = new TeacherScorePageItemDTO();
            item.setId(score.getId());
            item.setExamId(score.getExamId());
            item.setExamName(exam == null ? "" : exam.getExamName());
            item.setClassId(exam == null ? null : exam.getClassId());
            item.setClassName(clazz == null ? "" : clazz.getClassName());
            item.setCourseId(exam == null ? null : exam.getCourseId());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setStudentId(score.getStudentId());
            item.setStudentName(studentUser == null ? "" : studentUser.getRealName());
            item.setStudentNo(student == null ? "" : student.getStudentNo());
            item.setScore(score.getScore());
            item.setFullScore(exam == null ? BigDecimal.ZERO : exam.getFullScore());
            item.setRankInClass(score.getRankInClass());
            item.setTeacherComment(score.getTeacherComment());
            item.setPublishTime(score.getPublishTime());
            item.setCreatedAt(score.getCreatedAt());
            item.setUpdatedAt(score.getUpdatedAt());
            return item;
        }).collect(Collectors.toList());
    }
}
