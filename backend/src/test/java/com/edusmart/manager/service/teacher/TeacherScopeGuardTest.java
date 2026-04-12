package com.edusmart.manager.service.teacher;

import com.edusmart.manager.entity.EduAssignmentEntity;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduClassEntity;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class TeacherScopeGuardTest {

    @Mock
    private EduTeacherProfileMapper teacherProfileMapper;
    @Mock
    private EduClassMapper classMapper;
    @Mock
    private EduClassSessionMapper classSessionMapper;
    @Mock
    private EduClassStudentMapper classStudentMapper;
    @Mock
    private EduAttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private EduAttendanceExceptionMapper attendanceExceptionMapper;
    @Mock
    private EduAssignmentMapper assignmentMapper;
    @Mock
    private EduTeachingResourceMapper resourceMapper;
    @Mock
    private EduExamMapper examMapper;
    @Mock
    private EduExamScoreMapper scoreMapper;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TeacherScopeGuard guard;

    @Test
    void requireOwnedClassRejectsForeignClass() {
        mockCurrentTeacher(11L);
        EduClassEntity clazz = new EduClassEntity();
        clazz.setId(1L);
        clazz.setHeadTeacherId(22L);
        when(classMapper.selectById(1L)).thenReturn(clazz);

        assertThatThrownBy(() -> guard.requireOwnedClass(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void requireOwnedExamRejectsForeignExam() {
        mockCurrentTeacher(11L);
        EduExamEntity exam = new EduExamEntity();
        exam.setId(88L);
        exam.setTeacherId(22L);
        when(examMapper.selectById(88L)).thenReturn(exam);

        assertThatThrownBy(() -> guard.requireOwnedExam(88L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void requireOwnedScoreRejectsForeignScore() {
        mockCurrentTeacher(11L);
        EduExamScoreEntity score = new EduExamScoreEntity();
        score.setId(501L);
        score.setExamId(88L);
        when(scoreMapper.selectById(501L)).thenReturn(score);

        EduExamEntity exam = new EduExamEntity();
        exam.setId(88L);
        exam.setTeacherId(22L);
        when(examMapper.selectById(88L)).thenReturn(exam);

        assertThatThrownBy(() -> guard.requireOwnedScore(501L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void requireOwnedAssignmentRejectsForeignAssignment() {
        mockCurrentTeacher(11L);
        EduAssignmentEntity assignment = new EduAssignmentEntity();
        assignment.setId(301L);
        assignment.setTeacherId(22L);
        when(assignmentMapper.selectById(301L)).thenReturn(assignment);

        assertThatThrownBy(() -> guard.requireOwnedAssignment(301L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void requireOwnedResourceRejectsForeignResource() {
        mockCurrentTeacher(11L);
        EduTeachingResourceEntity resource = new EduTeachingResourceEntity();
        resource.setId(401L);
        resource.setTeacherId(22L);
        when(resourceMapper.selectById(401L)).thenReturn(resource);

        assertThatThrownBy(() -> guard.requireOwnedResource(401L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void requireOwnedAttendanceRecordRejectsForeignRecord() {
        mockCurrentTeacher(11L);
        EduAttendanceRecordEntity record = new EduAttendanceRecordEntity();
        record.setId(701L);
        record.setTeacherId(22L);
        when(attendanceRecordMapper.selectById(701L)).thenReturn(record);

        assertThatThrownBy(() -> guard.requireOwnedAttendanceRecord(701L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void requireOwnedAttendanceExceptionRejectsForeignException() {
        mockCurrentTeacher(11L);
        EduAttendanceExceptionEntity exception = new EduAttendanceExceptionEntity();
        exception.setId(601L);
        exception.setAttendanceRecordId(701L);
        when(attendanceExceptionMapper.selectById(601L)).thenReturn(exception);

        EduAttendanceRecordEntity record = new EduAttendanceRecordEntity();
        record.setId(701L);
        record.setTeacherId(22L);
        when(attendanceRecordMapper.selectById(701L)).thenReturn(record);

        assertThatThrownBy(() -> guard.requireOwnedAttendanceException(601L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    private void mockCurrentTeacher(Long teacherProfileId) {
        when(currentUserService.getCurrentUserId()).thenReturn(1001L);
        EduTeacherProfileEntity teacherProfile = new EduTeacherProfileEntity();
        teacherProfile.setId(teacherProfileId);
        teacherProfile.setUserId(1001L);
        when(teacherProfileMapper.selectOne(any())).thenReturn(teacherProfile);
    }
}
