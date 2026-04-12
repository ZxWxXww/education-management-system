package com.edusmart.manager.service.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edusmart.manager.entity.EduAssignmentEntity;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduClassSessionEntity;
import com.edusmart.manager.entity.EduClassStudentEntity;
import com.edusmart.manager.entity.EduExamEntity;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduTeachingResourceEntity;
import com.edusmart.manager.mapper.EduAssignmentMapper;
import com.edusmart.manager.mapper.EduAttendanceExceptionMapper;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassSessionMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.mapper.EduExamMapper;
import com.edusmart.manager.mapper.EduExamScoreMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduTeachingResourceMapper;
import com.edusmart.manager.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TeacherScopeGuard {
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduClassMapper classMapper;
    private final EduClassSessionMapper classSessionMapper;
    private final EduClassStudentMapper classStudentMapper;
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduAttendanceExceptionMapper attendanceExceptionMapper;
    private final EduAssignmentMapper assignmentMapper;
    private final EduTeachingResourceMapper resourceMapper;
    private final EduExamMapper examMapper;
    private final EduExamScoreMapper scoreMapper;
    private final CurrentUserService currentUserService;

    public TeacherScopeGuard(
            EduTeacherProfileMapper teacherProfileMapper,
            EduClassMapper classMapper,
            EduClassSessionMapper classSessionMapper,
            EduClassStudentMapper classStudentMapper,
            EduAttendanceRecordMapper attendanceRecordMapper,
            EduAttendanceExceptionMapper attendanceExceptionMapper,
            EduAssignmentMapper assignmentMapper,
            EduTeachingResourceMapper resourceMapper,
            EduExamMapper examMapper,
            EduExamScoreMapper scoreMapper,
            CurrentUserService currentUserService
    ) {
        this.teacherProfileMapper = teacherProfileMapper;
        this.classMapper = classMapper;
        this.classSessionMapper = classSessionMapper;
        this.classStudentMapper = classStudentMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.attendanceExceptionMapper = attendanceExceptionMapper;
        this.assignmentMapper = assignmentMapper;
        this.resourceMapper = resourceMapper;
        this.examMapper = examMapper;
        this.scoreMapper = scoreMapper;
        this.currentUserService = currentUserService;
    }

    public EduTeacherProfileEntity findCurrentTeacherProfile() {
        return teacherProfileMapper.selectOne(new LambdaQueryWrapper<EduTeacherProfileEntity>()
                .eq(EduTeacherProfileEntity::getUserId, currentUserService.getCurrentUserId())
                .last("limit 1"));
    }

    public EduTeacherProfileEntity requireCurrentTeacherProfile() {
        EduTeacherProfileEntity teacherProfile = findCurrentTeacherProfile();
        if (teacherProfile == null) {
            throw new ResponseStatusException(NOT_FOUND, "教师档案不存在");
        }
        return teacherProfile;
    }

    public Long requireCurrentTeacherProfileId() {
        return requireCurrentTeacherProfile().getId();
    }

    public EduClassEntity requireOwnedClass(Long classId) {
        EduClassEntity clazz = classMapper.selectById(classId);
        Long teacherProfileId = requireCurrentTeacherProfileId();
        if (clazz == null || !Objects.equals(clazz.getHeadTeacherId(), teacherProfileId)) {
            throw new ResponseStatusException(NOT_FOUND, "班级不存在");
        }
        return clazz;
    }

    public EduClassSessionEntity requireOwnedSession(Long sessionId) {
        EduClassSessionEntity session = classSessionMapper.selectById(sessionId);
        Long teacherProfileId = requireCurrentTeacherProfileId();
        if (session == null || !Objects.equals(session.getTeacherId(), teacherProfileId)) {
            throw new ResponseStatusException(NOT_FOUND, "课节不存在");
        }
        return session;
    }

    public EduAttendanceRecordEntity requireOwnedAttendanceRecord(Long recordId) {
        EduAttendanceRecordEntity record = attendanceRecordMapper.selectById(recordId);
        Long teacherProfileId = requireCurrentTeacherProfileId();
        if (record == null || !Objects.equals(record.getTeacherId(), teacherProfileId)) {
            throw new ResponseStatusException(NOT_FOUND, "考勤记录不存在");
        }
        return record;
    }

    public EduAttendanceExceptionEntity requireOwnedAttendanceException(Long exceptionId) {
        EduAttendanceExceptionEntity exception = attendanceExceptionMapper.selectById(exceptionId);
        if (exception == null) {
            throw new ResponseStatusException(NOT_FOUND, "考勤异常不存在");
        }
        requireOwnedAttendanceRecord(exception.getAttendanceRecordId());
        return exception;
    }

    public EduAssignmentEntity requireOwnedAssignment(Long assignmentId) {
        EduAssignmentEntity assignment = assignmentMapper.selectById(assignmentId);
        Long teacherProfileId = requireCurrentTeacherProfileId();
        if (assignment == null || !Objects.equals(assignment.getTeacherId(), teacherProfileId)) {
            throw new ResponseStatusException(NOT_FOUND, "作业不存在");
        }
        return assignment;
    }

    public EduTeachingResourceEntity requireOwnedResource(Long resourceId) {
        EduTeachingResourceEntity resource = resourceMapper.selectById(resourceId);
        Long teacherProfileId = requireCurrentTeacherProfileId();
        if (resource == null || !Objects.equals(resource.getTeacherId(), teacherProfileId)) {
            throw new ResponseStatusException(NOT_FOUND, "资源不存在");
        }
        return resource;
    }

    public EduExamEntity requireOwnedExam(Long examId) {
        EduExamEntity exam = examMapper.selectById(examId);
        Long teacherProfileId = requireCurrentTeacherProfileId();
        if (exam == null || !Objects.equals(exam.getTeacherId(), teacherProfileId)) {
            throw new ResponseStatusException(NOT_FOUND, "考试不存在");
        }
        return exam;
    }

    public EduExamScoreEntity requireOwnedScore(Long scoreId) {
        EduExamScoreEntity score = scoreMapper.selectById(scoreId);
        if (score == null) {
            throw new ResponseStatusException(NOT_FOUND, "成绩不存在");
        }
        requireOwnedExam(score.getExamId());
        return score;
    }

    public void requireStudentInClass(Long studentId, Long classId) {
        if (studentId == null || classId == null) {
            return;
        }
        EduClassStudentEntity relation = classStudentMapper.selectOne(new LambdaQueryWrapper<EduClassStudentEntity>()
                .eq(EduClassStudentEntity::getClassId, classId)
                .eq(EduClassStudentEntity::getStudentId, studentId)
                .eq(EduClassStudentEntity::getStatus, 1)
                .last("limit 1"));
        if (relation == null) {
            throw new ResponseStatusException(NOT_FOUND, "学员不属于当前班级");
        }
    }
}
