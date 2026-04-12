package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAssignmentPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAssignmentPageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAssignmentSaveDTO;
import com.edusmart.manager.entity.EduAssignmentEntity;
import com.edusmart.manager.entity.EduAssignmentSubmissionEntity;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduClassStudentEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.mapper.EduAssignmentMapper;
import com.edusmart.manager.mapper.EduAssignmentSubmissionMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.teacher.TeacherAssignmentService;
import com.edusmart.manager.service.teacher.TeacherScopeGuard;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {
    private final EduAssignmentMapper assignmentMapper;
    private final EduAssignmentSubmissionMapper submissionMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduClassStudentMapper classStudentMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final CurrentUserService currentUserService;
    private final TeacherScopeGuard teacherScopeGuard;

    public TeacherAssignmentServiceImpl(
            EduAssignmentMapper assignmentMapper,
            EduAssignmentSubmissionMapper submissionMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduClassStudentMapper classStudentMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            CurrentUserService currentUserService,
            TeacherScopeGuard teacherScopeGuard
    ) {
        this.assignmentMapper = assignmentMapper;
        this.submissionMapper = submissionMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.classStudentMapper = classStudentMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.currentUserService = currentUserService;
        this.teacherScopeGuard = teacherScopeGuard;
    }

    @Override
    public PageData<TeacherAssignmentPageItemDTO> page(TeacherAssignmentPageQueryDTO queryDTO) {
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (teacherProfile == null) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }
        QueryWrapper<EduAssignmentEntity> w = new QueryWrapper<>();
        w.eq("teacher_id", teacherProfile.getId());
        if (StringUtils.hasText(queryDTO.getKeyword())) w.like("title", queryDTO.getKeyword());
        if (queryDTO.getClassId() != null) w.eq("class_id", queryDTO.getClassId());
        if (queryDTO.getCourseId() != null) w.eq("course_id", queryDTO.getCourseId());
        if (StringUtils.hasText(queryDTO.getStatus())) w.eq("status", queryDTO.getStatus());
        w.orderByDesc("id");
        Page<EduAssignmentEntity> p = assignmentMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), buildItems(p.getRecords()));
    }

    @Override
    public TeacherAssignmentPageItemDTO getById(Long id) {
        EduAssignmentEntity assignment = assignmentMapper.selectById(id);
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (assignment == null || teacherProfile == null || !Objects.equals(assignment.getTeacherId(), teacherProfile.getId())) {
            throw new ResponseStatusException(NOT_FOUND, "作业不存在");
        }
        return buildItems(Collections.singletonList(assignment)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "作业不存在"));
    }

    @Override
    public Long create(TeacherAssignmentSaveDTO dto) {
        EduTeacherProfileEntity teacherProfile = teacherScopeGuard.requireCurrentTeacherProfile();
        teacherScopeGuard.requireOwnedClass(dto.getClassId());
        EduAssignmentEntity e = new EduAssignmentEntity();
        fill(e, dto, teacherProfile.getId());
        assignmentMapper.insert(e);
        return e.getId();
    }

    @Override
    public void update(Long id, TeacherAssignmentSaveDTO dto) {
        EduAssignmentEntity e = teacherScopeGuard.requireOwnedAssignment(id);
        EduTeacherProfileEntity teacherProfile = teacherScopeGuard.requireCurrentTeacherProfile();
        teacherScopeGuard.requireOwnedClass(dto.getClassId());
        fill(e, dto, teacherProfile.getId());
        assignmentMapper.updateById(e);
    }

    @Override
    public void delete(Long id) {
        teacherScopeGuard.requireOwnedAssignment(id);
        assignmentMapper.deleteById(id);
    }

    private void fill(EduAssignmentEntity e, TeacherAssignmentSaveDTO dto, Long teacherId) {
        e.setClassId(dto.getClassId());
        e.setCourseId(dto.getCourseId());
        e.setTeacherId(teacherId);
        e.setTitle(dto.getTitle());
        e.setContent(dto.getContent());
        e.setAttachmentType(dto.getAttachmentType());
        e.setAttachmentUrl(dto.getAttachmentUrl());
        e.setPublishTime(dto.getPublishTime());
        e.setDeadline(dto.getDeadline());
        e.setStatus(dto.getStatus());
    }

    private EduTeacherProfileEntity getCurrentTeacherProfile() {
        return teacherProfileMapper.selectOne(new QueryWrapper<EduTeacherProfileEntity>()
                .eq("user_id", currentUserService.getCurrentUserId()));
    }

    private List<TeacherAssignmentPageItemDTO> buildItems(List<EduAssignmentEntity> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> classIds = assignments.stream().map(EduAssignmentEntity::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> courseIds = assignments.stream().map(EduAssignmentEntity::getCourseId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> assignmentIds = assignments.stream().map(EduAssignmentEntity::getId).filter(Objects::nonNull).collect(Collectors.toList());

        Map<Long, EduClassEntity> classMap = classIds.isEmpty() ? Collections.emptyMap() :
                classMapper.selectBatchIds(classIds).stream().collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduCourseEntity> courseMap = courseIds.isEmpty() ? Collections.emptyMap() :
                courseMapper.selectBatchIds(courseIds).stream().collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> right));
        Map<Long, Integer> classTotalMap = classIds.isEmpty() ? Collections.emptyMap() :
                classStudentMapper.selectList(new QueryWrapper<EduClassStudentEntity>().in("class_id", classIds).eq("status", 1)).stream()
                        .collect(Collectors.groupingBy(EduClassStudentEntity::getClassId, Collectors.summingInt(item -> 1)));
        Map<Long, Integer> submissionCountMap = assignmentIds.isEmpty() ? Collections.emptyMap() :
                submissionMapper.selectList(new QueryWrapper<EduAssignmentSubmissionEntity>().in("assignment_id", assignmentIds)).stream()
                        .collect(Collectors.groupingBy(EduAssignmentSubmissionEntity::getAssignmentId, Collectors.summingInt(item -> 1)));

        return assignments.stream().map(assignment -> {
            TeacherAssignmentPageItemDTO item = new TeacherAssignmentPageItemDTO();
            item.setId(assignment.getId());
            item.setClassId(assignment.getClassId());
            item.setCourseId(assignment.getCourseId());
            item.setTeacherId(assignment.getTeacherId());
            item.setTitle(assignment.getTitle());
            item.setContent(assignment.getContent());
            item.setAttachmentType(assignment.getAttachmentType());
            item.setAttachmentUrl(assignment.getAttachmentUrl());
            item.setPublishTime(assignment.getPublishTime());
            item.setDeadline(assignment.getDeadline());
            item.setStatus(assignment.getStatus());
            item.setCreatedAt(assignment.getCreatedAt());
            item.setUpdatedAt(assignment.getUpdatedAt());
            item.setSubmitted(submissionCountMap.getOrDefault(assignment.getId(), 0));
            item.setTotal(classTotalMap.getOrDefault(assignment.getClassId(), 0));
            item.setAvgDuration("--");
            EduClassEntity clazz = classMap.get(assignment.getClassId());
            EduCourseEntity course = courseMap.get(assignment.getCourseId());
            item.setClassName(clazz == null ? "" : clazz.getClassName());
            item.setCourseName(course == null ? "" : course.getCourseName());
            return item;
        }).collect(Collectors.toList());
    }
}
