package com.edusmart.manager.service.teacher;

import com.edusmart.manager.dto.teacher.TeacherScoreSaveDTO;
import com.edusmart.manager.entity.EduExamEntity;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduExamMapper;
import com.edusmart.manager.mapper.EduExamScoreMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.teacher.impl.TeacherScoreServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class TeacherScoreServiceImplTest {

    @Mock
    private EduExamScoreMapper scoreMapper;
    @Mock
    private EduExamMapper examMapper;
    @Mock
    private EduClassMapper classMapper;
    @Mock
    private EduCourseMapper courseMapper;
    @Mock
    private EduStudentProfileMapper studentProfileMapper;
    @Mock
    private EduTeacherProfileMapper teacherProfileMapper;
    @Mock
    private EduUserMapper userMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private TeacherScopeGuard teacherScopeGuard;

    @InjectMocks
    private TeacherScoreServiceImpl service;

    @Test
    void createRejectsExamOutsideCurrentTeacherScope() {
        when(teacherScopeGuard.requireOwnedExam(88L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "考试不存在"));

        TeacherScoreSaveDTO dto = new TeacherScoreSaveDTO();
        dto.setExamId(88L);
        dto.setStudentId(301L);
        dto.setScore(BigDecimal.valueOf(96));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void deleteRejectsScoreOutsideCurrentTeacherScope() {
        when(teacherScopeGuard.requireOwnedScore(501L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "成绩不存在"));

        assertThatThrownBy(() -> service.delete(501L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void analysisRejectsExamOutsideCurrentTeacherScope() {
        when(teacherScopeGuard.requireOwnedExam(88L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "考试不存在"));

        assertThatThrownBy(() -> service.analysis(88L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }
}
