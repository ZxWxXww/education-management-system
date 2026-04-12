package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminScorePageItemDTO;
import com.edusmart.manager.dto.admin.AdminScorePageQueryDTO;
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
import com.edusmart.manager.service.admin.AdminScoreService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminScoreServiceImpl implements AdminScoreService {
    private final EduExamScoreMapper examScoreMapper;
    private final EduExamMapper examMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;

    public AdminScoreServiceImpl(
            EduExamScoreMapper examScoreMapper,
            EduExamMapper examMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper
    ) {
        this.examScoreMapper = examScoreMapper;
        this.examMapper = examMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageData<AdminScorePageItemDTO> page(AdminScorePageQueryDTO queryDTO) {
        List<EduExamScoreEntity> scores = examScoreMapper.selectList(new QueryWrapper<EduExamScoreEntity>().orderByDesc("publish_time", "id"));
        if (scores.isEmpty()) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }

        Map<Long, EduExamEntity> examMap = examMapper.selectBatchIds(
                        scores.stream().map(EduExamScoreEntity::getExamId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduExamEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduStudentProfileEntity> studentMap = studentProfileMapper.selectBatchIds(
                        scores.stream().map(EduExamScoreEntity::getStudentId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduStudentProfileEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(
                        examMap.values().stream().map(EduExamEntity::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduCourseEntity> courseMap = courseMapper.selectBatchIds(
                        examMap.values().stream().map(EduExamEntity::getCourseId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherProfileMapper.selectBatchIds(
                        examMap.values().stream().map(EduExamEntity::getTeacherId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> left));

        List<Long> userIds = studentMap.values().stream().map(EduStudentProfileEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toList());
        userIds.addAll(teacherMap.values().stream().map(EduTeacherProfileEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toList()));
        Map<Long, EduUserEntity> userMap = userMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> left));

        List<AdminScorePageItemDTO> rows = scores.stream().map(item -> {
            EduExamEntity exam = examMap.get(item.getExamId());
            EduStudentProfileEntity student = studentMap.get(item.getStudentId());
            EduUserEntity studentUser = student == null ? null : userMap.get(student.getUserId());
            EduCourseEntity course = exam == null ? null : courseMap.get(exam.getCourseId());
            EduClassEntity clazz = exam == null ? null : classMap.get(exam.getClassId());
            EduTeacherProfileEntity teacher = exam == null ? null : teacherMap.get(exam.getTeacherId());
            EduUserEntity teacherUser = teacher == null ? null : userMap.get(teacher.getUserId());

            AdminScorePageItemDTO dto = new AdminScorePageItemDTO();
            dto.setId(item.getId());
            dto.setCourseId(exam == null ? null : exam.getCourseId());
            dto.setStudentName(studentUser == null ? "" : studentUser.getRealName());
            dto.setStudentNo(student == null ? "" : student.getStudentNo());
            dto.setCourseName(course == null ? "" : course.getCourseName());
            dto.setClassName(clazz == null ? "" : clazz.getClassName());
            dto.setExamName(exam == null ? "" : exam.getExamName());
            dto.setScore(item.getScore());
            dto.setRankInClass(item.getRankInClass());
            dto.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            dto.setPublishTime(item.getPublishTime());
            dto.setCreatedAt(item.getCreatedAt());
            dto.setUpdatedAt(item.getUpdatedAt());
            return dto;
        }).filter(item -> matches(item, queryDTO)).collect(Collectors.toList());

        return paginate(rows, queryDTO.getCurrent(), queryDTO.getSize());
    }

    private boolean matches(AdminScorePageItemDTO item, AdminScorePageQueryDTO queryDTO) {
        boolean matchCourse = queryDTO.getCourseId() == null || Objects.equals(queryDTO.getCourseId(), item.getCourseId());
        boolean matchKeyword = queryDTO.getKeyword() == null || queryDTO.getKeyword().isBlank()
                || contains(item.getStudentName(), queryDTO.getKeyword())
                || contains(item.getStudentNo(), queryDTO.getKeyword())
                || contains(item.getExamName(), queryDTO.getKeyword())
                || contains(item.getCourseName(), queryDTO.getKeyword());
        return matchCourse && matchKeyword;
    }

    private boolean contains(String source, String keyword) {
        return source != null && keyword != null && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private PageData<AdminScorePageItemDTO> paginate(List<AdminScorePageItemDTO> rows, long current, long size) {
        int fromIndex = (int) Math.max(0, (current - 1) * size);
        int toIndex = (int) Math.min(rows.size(), fromIndex + size);
        List<AdminScorePageItemDTO> records = fromIndex >= rows.size() ? Collections.emptyList() : rows.subList(fromIndex, toIndex);
        return new PageData<>(current, size, rows.size(), records);
    }
}
