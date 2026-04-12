package com.edusmart.manager.service.teacher;

import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.mapper.EduAttendanceExceptionMapper;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassSessionMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.teacher.impl.TeacherAttendanceExceptionServiceImpl;
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
class TeacherAttendanceExceptionServiceImplTest {

    @Mock
    private EduAttendanceExceptionMapper mapper;
    @Mock
    private EduAttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private EduTeacherProfileMapper teacherProfileMapper;
    @Mock
    private EduStudentProfileMapper studentProfileMapper;
    @Mock
    private EduClassMapper classMapper;
    @Mock
    private EduCourseMapper courseMapper;
    @Mock
    private EduClassSessionMapper classSessionMapper;
    @Mock
    private EduUserMapper userMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private TeacherScopeGuard teacherScopeGuard;

    @InjectMocks
    private TeacherAttendanceExceptionServiceImpl service;

    @Test
    void deleteRejectsExceptionOutsideCurrentTeacherScope() {
        when(teacherScopeGuard.requireOwnedAttendanceException(601L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "考勤异常不存在"));

        assertThatThrownBy(() -> service.delete(601L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }
}
