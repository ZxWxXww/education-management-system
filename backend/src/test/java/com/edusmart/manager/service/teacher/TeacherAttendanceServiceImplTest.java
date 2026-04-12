package com.edusmart.manager.service.teacher;

import com.edusmart.manager.dto.teacher.TeacherAttendanceSaveDTO;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduClassSessionEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.mapper.EduAttendanceBatchTaskMapper;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassSessionMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.HourPackageLedgerService;
import com.edusmart.manager.service.teacher.impl.TeacherAttendanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class TeacherAttendanceServiceImplTest {

    @Mock
    private EduAttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private EduAttendanceBatchTaskMapper batchTaskMapper;
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
    @Mock
    private HourPackageLedgerService hourPackageLedgerService;

    @InjectMocks
    private TeacherAttendanceServiceImpl service;

    @Test
    void updateRejectsAttendanceOutsideCurrentTeacherScope() {
        when(teacherScopeGuard.requireOwnedAttendanceRecord(501L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "attendance not found"));

        TeacherAttendanceSaveDTO dto = new TeacherAttendanceSaveDTO();
        dto.setClassId(1L);
        dto.setStudentId(2L);
        dto.setAttendanceDate(LocalDate.of(2026, 4, 11));
        dto.setStatus("PRESENT");

        assertThatThrownBy(() -> service.update(501L, dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void createShouldRefreshHourDeductionAfterPersistingAttendance() {
        EduTeacherProfileEntity teacherProfile = new EduTeacherProfileEntity();
        teacherProfile.setId(88L);
        EduClassSessionEntity session = new EduClassSessionEntity();
        session.setId(9L);
        session.setClassId(1L);
        when(teacherScopeGuard.requireCurrentTeacherProfile()).thenReturn(teacherProfile);
        when(teacherScopeGuard.requireOwnedSession(9L)).thenReturn(session);

        TeacherAttendanceSaveDTO dto = new TeacherAttendanceSaveDTO();
        dto.setClassId(1L);
        dto.setSessionId(9L);
        dto.setStudentId(2L);
        dto.setAttendanceDate(LocalDate.of(2026, 4, 12));
        dto.setStatus("PRESENT");

        service.create(dto);

        verify(attendanceRecordMapper).insert(any(EduAttendanceRecordEntity.class));
        verify(hourPackageLedgerService).refreshAttendanceDeduction(any(EduAttendanceRecordEntity.class));
    }

    @Test
    void deleteShouldRollbackHourDeductionBeforeRemovingAttendance() {
        EduAttendanceRecordEntity record = new EduAttendanceRecordEntity();
        record.setId(19L);
        when(teacherScopeGuard.requireOwnedAttendanceRecord(19L)).thenReturn(record);

        service.delete(19L);

        verify(hourPackageLedgerService).rollbackAttendanceDeduction(record);
        verify(attendanceRecordMapper).deleteById(19L);
    }
}
