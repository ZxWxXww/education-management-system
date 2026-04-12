package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminAssignmentPageItemDTO;
import com.edusmart.manager.dto.admin.AdminAssignmentPageQueryDTO;
import com.edusmart.manager.entity.EduAssignmentEntity;
import com.edusmart.manager.entity.EduAssignmentSubmissionEntity;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduClassStudentEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduAssignmentMapper;
import com.edusmart.manager.mapper.EduAssignmentSubmissionMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.service.admin.AdminAssignmentService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminAssignmentServiceImpl implements AdminAssignmentService {
    private final EduAssignmentMapper assignmentMapper;
    private final EduAssignmentSubmissionMapper assignmentSubmissionMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final EduClassStudentMapper classStudentMapper;

    public AdminAssignmentServiceImpl(
            EduAssignmentMapper assignmentMapper,
            EduAssignmentSubmissionMapper assignmentSubmissionMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            EduClassStudentMapper classStudentMapper
    ) {
        this.assignmentMapper = assignmentMapper;
        this.assignmentSubmissionMapper = assignmentSubmissionMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.classStudentMapper = classStudentMapper;
    }

    @Override
    public PageData<AdminAssignmentPageItemDTO> page(AdminAssignmentPageQueryDTO queryDTO) {
        List<EduAssignmentEntity> assignments = assignmentMapper.selectList(new QueryWrapper<EduAssignmentEntity>().orderByDesc("publish_time", "id"));
        if (assignments.isEmpty()) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }

        List<Long> classIds = assignments.stream().map(EduAssignmentEntity::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> courseIds = assignments.stream().map(EduAssignmentEntity::getCourseId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> teacherIds = assignments.stream().map(EduAssignmentEntity::getTeacherId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> assignmentIds = assignments.stream().map(EduAssignmentEntity::getId).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(classIds).stream()
                .collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduCourseEntity> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherProfileMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduUserEntity> userMap = userMapper.selectBatchIds(
                        teacherMap.values().stream().map(EduTeacherProfileEntity::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> left));
        Map<Long, Long> classStudentCountMap = classStudentMapper.selectList(new QueryWrapper<EduClassStudentEntity>()
                        .in(!classIds.isEmpty(), "class_id", classIds)
                        .eq("status", 1))
                .stream()
                .collect(Collectors.groupingBy(EduClassStudentEntity::getClassId, Collectors.counting()));
        Map<Long, Long> submissionCountMap = assignmentSubmissionMapper.selectList(new QueryWrapper<EduAssignmentSubmissionEntity>()
                        .in(!assignmentIds.isEmpty(), "assignment_id", assignmentIds))
                .stream()
                .collect(Collectors.groupingBy(EduAssignmentSubmissionEntity::getAssignmentId, Collectors.counting()));

        List<AdminAssignmentPageItemDTO> rows = assignments.stream().map(item -> {
            EduClassEntity clazz = classMap.get(item.getClassId());
            EduCourseEntity course = courseMap.get(item.getCourseId());
            EduTeacherProfileEntity teacher = teacherMap.get(item.getTeacherId());
            EduUserEntity teacherUser = teacher == null ? null : userMap.get(teacher.getUserId());

            AdminAssignmentPageItemDTO dto = new AdminAssignmentPageItemDTO();
            dto.setId(item.getId());
            dto.setCourseId(item.getCourseId());
            dto.setTitle(item.getTitle());
            dto.setCourseName(course == null ? "" : course.getCourseName());
            dto.setClassName(clazz == null ? "" : clazz.getClassName());
            dto.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            dto.setDeadline(item.getDeadline());
            dto.setStatus(item.getStatus());
            dto.setSubmitted(submissionCountMap.getOrDefault(item.getId(), 0L));
            dto.setTotal(classStudentCountMap.getOrDefault(item.getClassId(), 0L));
            dto.setCreatedAt(item.getCreatedAt());
            dto.setUpdatedAt(item.getUpdatedAt());
            return dto;
        }).filter(item -> matches(item, queryDTO)).collect(Collectors.toList());

        return paginate(rows, queryDTO.getCurrent(), queryDTO.getSize());
    }

    private boolean matches(AdminAssignmentPageItemDTO item, AdminAssignmentPageQueryDTO queryDTO) {
        boolean matchCourse = queryDTO.getCourseId() == null || Objects.equals(queryDTO.getCourseId(), item.getCourseId());
        boolean matchStatus = queryDTO.getStatus() == null || queryDTO.getStatus().isBlank() || queryDTO.getStatus().equalsIgnoreCase(item.getStatus());
        boolean matchKeyword = queryDTO.getKeyword() == null || queryDTO.getKeyword().isBlank()
                || contains(item.getTitle(), queryDTO.getKeyword())
                || contains(String.valueOf(item.getId()), queryDTO.getKeyword())
                || contains(item.getTeacherName(), queryDTO.getKeyword())
                || contains(item.getCourseName(), queryDTO.getKeyword());
        return matchCourse && matchStatus && matchKeyword;
    }

    private boolean contains(String source, String keyword) {
        return source != null && keyword != null && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private PageData<AdminAssignmentPageItemDTO> paginate(List<AdminAssignmentPageItemDTO> rows, long current, long size) {
        int fromIndex = (int) Math.max(0, (current - 1) * size);
        int toIndex = (int) Math.min(rows.size(), fromIndex + size);
        List<AdminAssignmentPageItemDTO> records = fromIndex >= rows.size() ? Collections.emptyList() : rows.subList(fromIndex, toIndex);
        return new PageData<>(current, size, rows.size(), records);
    }
}
