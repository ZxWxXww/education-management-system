package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentAssignmentPageItemDTO;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionSaveDTO;
import com.edusmart.manager.entity.EduAssignmentEntity;
import com.edusmart.manager.entity.EduAssignmentSubmissionEntity;
import com.edusmart.manager.entity.EduClassStudentEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduAssignmentMapper;
import com.edusmart.manager.mapper.EduAssignmentSubmissionMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.student.StudentAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudentAssignmentServiceImpl implements StudentAssignmentService {
    private final EduAssignmentMapper assignmentMapper;
    private final EduAssignmentSubmissionMapper submissionMapper;
    private final EduClassStudentMapper classStudentMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduCourseMapper courseMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final CurrentUserService currentUserService;

    public StudentAssignmentServiceImpl(
            EduAssignmentMapper assignmentMapper,
            EduAssignmentSubmissionMapper submissionMapper,
            EduClassStudentMapper classStudentMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduCourseMapper courseMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            CurrentUserService currentUserService
    ) {
        this.assignmentMapper = assignmentMapper;
        this.submissionMapper = submissionMapper;
        this.classStudentMapper = classStudentMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.courseMapper = courseMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public PageData<StudentAssignmentPageItemDTO> pageSubmissions(StudentAssignmentSubmissionPageQueryDTO queryDTO) {
        Long studentProfileId = getCurrentStudentProfileId();
        List<Long> classIds = classStudentMapper.selectList(new QueryWrapper<EduClassStudentEntity>()
                        .eq("student_id", studentProfileId)
                        .eq("status", 1))
                .stream()
                .map(EduClassStudentEntity::getClassId)
                .distinct()
                .collect(Collectors.toList());
        if (classIds.isEmpty()) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }

        QueryWrapper<EduAssignmentEntity> assignmentWrapper = new QueryWrapper<EduAssignmentEntity>()
                .in("class_id", classIds)
                .orderByDesc("id");
        if (queryDTO.getAssignmentId() != null) {
            assignmentWrapper.eq("id", queryDTO.getAssignmentId());
        }

        Page<EduAssignmentEntity> assignmentPage = assignmentMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                assignmentWrapper
        );
        List<StudentAssignmentPageItemDTO> items = buildItems(assignmentPage.getRecords(), studentProfileId)
                .stream()
                .filter(item -> !hasStatusFilter(queryDTO.getStatus()) || queryDTO.getStatus().equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList());
        return new PageData<>(assignmentPage.getCurrent(), assignmentPage.getSize(), assignmentPage.getTotal(), items);
    }

    @Override
    public StudentAssignmentPageItemDTO getSubmission(Long id) {
        EduAssignmentEntity assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            throw new ResponseStatusException(NOT_FOUND, "作业不存在");
        }
        Long studentProfileId = getCurrentStudentProfileId();
        if (!isStudentInClass(studentProfileId, assignment.getClassId())) {
            throw new ResponseStatusException(NOT_FOUND, "作业不存在");
        }
        return buildItems(Collections.singletonList(assignment), studentProfileId).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "作业不存在"));
    }

    @Override
    public Long createSubmission(StudentAssignmentSubmissionSaveDTO dto) {
        EduAssignmentSubmissionEntity e = new EduAssignmentSubmissionEntity();
        fill(e, dto, getCurrentStudentProfileId());
        if (e.getSubmitTime() == null) e.setSubmitTime(LocalDateTime.now());
        submissionMapper.insert(e);
        return e.getId();
    }

    @Override
    public void updateSubmission(Long id, StudentAssignmentSubmissionSaveDTO dto) {
        EduAssignmentSubmissionEntity e = submissionMapper.selectById(id);
        if (e == null) return;
        Long studentProfileId = getCurrentStudentProfileId();
        if (!Objects.equals(e.getStudentId(), studentProfileId)) return;
        fill(e, dto, studentProfileId);
        if (e.getSubmitTime() == null) e.setSubmitTime(LocalDateTime.now());
        submissionMapper.updateById(e);
    }

    @Override
    public void deleteSubmission(Long id) {
        EduAssignmentSubmissionEntity e = submissionMapper.selectById(id);
        if (e == null || !Objects.equals(e.getStudentId(), getCurrentStudentProfileId())) return;
        submissionMapper.deleteById(id);
    }

    private void fill(EduAssignmentSubmissionEntity e, StudentAssignmentSubmissionSaveDTO dto, Long studentProfileId) {
        e.setAssignmentId(dto.getAssignmentId());
        e.setStudentId(studentProfileId);
        e.setSubmitTime(dto.getSubmitTime());
        e.setSubmitContent(dto.getSubmitContent());
        e.setAttachmentType(dto.getAttachmentType());
        e.setAttachmentUrl(dto.getAttachmentUrl());
        e.setStatus(dto.getStatus());
    }

    private Long getCurrentStudentProfileId() {
        EduStudentProfileEntity profile = studentProfileMapper.selectOne(new QueryWrapper<EduStudentProfileEntity>()
                .eq("user_id", currentUserService.getCurrentUserId()));
        if (profile == null) {
            throw new ResponseStatusException(NOT_FOUND, "学生档案不存在");
        }
        return profile.getId();
    }

    private boolean isStudentInClass(Long studentProfileId, Long classId) {
        return classStudentMapper.selectCount(new QueryWrapper<EduClassStudentEntity>()
                .eq("student_id", studentProfileId)
                .eq("class_id", classId)
                .eq("status", 1)) > 0;
    }

    private boolean hasStatusFilter(String status) {
        return status != null && !status.isBlank();
    }

    private List<StudentAssignmentPageItemDTO> buildItems(List<EduAssignmentEntity> assignments, Long studentProfileId) {
        if (assignments == null || assignments.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> assignmentIds = assignments.stream().map(EduAssignmentEntity::getId).filter(Objects::nonNull).collect(Collectors.toList());
        List<Long> courseIds = assignments.stream().map(EduAssignmentEntity::getCourseId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> teacherIds = assignments.stream().map(EduAssignmentEntity::getTeacherId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, EduAssignmentSubmissionEntity> submissionMap = submissionMapper.selectList(new QueryWrapper<EduAssignmentSubmissionEntity>()
                        .in("assignment_id", assignmentIds)
                        .eq("student_id", studentProfileId))
                .stream()
                .collect(Collectors.toMap(EduAssignmentSubmissionEntity::getAssignmentId, item -> item, (left, right) -> right));
        Map<Long, EduCourseEntity> courseMap = courseIds.isEmpty() ? Collections.emptyMap() :
                courseMapper.selectBatchIds(courseIds).stream().collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherIds.isEmpty() ? Collections.emptyMap() :
                teacherProfileMapper.selectBatchIds(teacherIds).stream().collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduUserEntity> userMap = teacherMap.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(teacherMap.values().stream().map(EduTeacherProfileEntity::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                        .stream()
                        .collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right));

        return assignments.stream().map(assignment -> {
            EduAssignmentSubmissionEntity submission = submissionMap.get(assignment.getId());
            EduCourseEntity course = courseMap.get(assignment.getCourseId());
            EduTeacherProfileEntity teacherProfile = teacherMap.get(assignment.getTeacherId());
            EduUserEntity teacherUser = teacherProfile == null ? null : userMap.get(teacherProfile.getUserId());
            StudentAssignmentPageItemDTO item = new StudentAssignmentPageItemDTO();
            item.setId(assignment.getId());
            item.setSubmissionId(submission == null ? null : submission.getId());
            item.setAssignmentTitle(assignment.getTitle());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            item.setDueTime(assignment.getDeadline());
            item.setSubmitTime(submission == null ? null : submission.getSubmitTime());
            item.setScore(submission == null ? null : submission.getScore());
            item.setFeedback(submission == null ? "尚未提交" : submission.getFeedback());
            item.setCreatedAt(submission == null ? assignment.getCreatedAt() : submission.getCreatedAt());
            item.setUpdatedAt(submission == null ? assignment.getUpdatedAt() : submission.getUpdatedAt());
            item.setStatus(deriveStatus(submission));
            return item;
        }).collect(Collectors.toList());
    }

    private String deriveStatus(EduAssignmentSubmissionEntity submission) {
        if (submission == null || submission.getStatus() == null) {
            return "pending";
        }
        String status = submission.getStatus().toUpperCase();
        if ("LATE".equals(status)) return "late";
        if ("PENDING".equals(status)) return "pending";
        return "submitted";
    }
}
