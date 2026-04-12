package com.edusmart.manager.service.teacher;

import com.edusmart.manager.dto.teacher.TeacherAssignmentSaveDTO;
import com.edusmart.manager.entity.EduAssignmentEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.mapper.EduAssignmentMapper;
import com.edusmart.manager.mapper.EduAssignmentSubmissionMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.teacher.impl.TeacherAssignmentServiceImpl;
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
class TeacherAssignmentServiceImplTest {

    @Mock
    private EduAssignmentMapper assignmentMapper;
    @Mock
    private EduAssignmentSubmissionMapper submissionMapper;
    @Mock
    private EduClassMapper classMapper;
    @Mock
    private EduCourseMapper courseMapper;
    @Mock
    private EduClassStudentMapper classStudentMapper;
    @Mock
    private EduTeacherProfileMapper teacherProfileMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private TeacherScopeGuard teacherScopeGuard;

    @InjectMocks
    private TeacherAssignmentServiceImpl service;

    @Test
    void updateRejectsAssignmentOutsideCurrentTeacherScope() {
        when(teacherScopeGuard.requireOwnedAssignment(301L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "作业不存在"));

        TeacherAssignmentSaveDTO dto = new TeacherAssignmentSaveDTO();
        dto.setClassId(1L);
        dto.setCourseId(2L);
        dto.setTeacherId(11L);
        dto.setTitle("周练");

        assertThatThrownBy(() -> service.update(301L, dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }
}
