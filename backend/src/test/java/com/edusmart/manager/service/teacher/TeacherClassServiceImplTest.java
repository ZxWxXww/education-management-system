package com.edusmart.manager.service.teacher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.dto.teacher.TeacherClassPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherClassSaveDTO;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassSessionMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduExamMapper;
import com.edusmart.manager.mapper.EduExamScoreMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.teacher.impl.TeacherClassServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherClassServiceImplTest {

    @Mock
    private EduClassMapper classMapper;
    @Mock
    private EduCourseMapper courseMapper;
    @Mock
    private EduTeacherProfileMapper teacherProfileMapper;
    @Mock
    private EduUserMapper userMapper;
    @Mock
    private EduClassStudentMapper classStudentMapper;
    @Mock
    private EduClassSessionMapper classSessionMapper;
    @Mock
    private EduAttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private EduStudentProfileMapper studentProfileMapper;
    @Mock
    private EduExamMapper examMapper;
    @Mock
    private EduExamScoreMapper examScoreMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private TeacherScopeGuard teacherScopeGuard;

    @InjectMocks
    private TeacherClassServiceImpl service;

    @Test
    void pageAddsCurrentTeacherConstraint() {
        EduTeacherProfileEntity teacherProfile = teacherProfile(11L);
        when(teacherScopeGuard.findCurrentTeacherProfile()).thenReturn(teacherProfile);
        when(classMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(new Page<>());

        TeacherClassPageQueryDTO queryDTO = new TeacherClassPageQueryDTO();
        queryDTO.setCurrent(1L);
        queryDTO.setSize(10L);

        service.page(queryDTO);

        ArgumentCaptor<QueryWrapper> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(classMapper).selectPage(any(Page.class), wrapperCaptor.capture());
        QueryWrapper<?> wrapper = wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSegment()).contains("head_teacher_id");
    }

    @Test
    void createAssignsCurrentTeacherAsHeadTeacher() {
        EduTeacherProfileEntity teacherProfile = teacherProfile(11L);
        when(teacherScopeGuard.requireCurrentTeacherProfile()).thenReturn(teacherProfile);

        TeacherClassSaveDTO dto = new TeacherClassSaveDTO();
        dto.setClassCode("CLS-001");
        dto.setClassName("高一数学班");
        dto.setCourseId(21L);
        dto.setStartDate(LocalDate.of(2026, 4, 11));
        dto.setEndDate(LocalDate.of(2026, 8, 31));
        dto.setStatus(1);

        service.create(dto);

        ArgumentCaptor<com.edusmart.manager.entity.EduClassEntity> entityCaptor =
                ArgumentCaptor.forClass(com.edusmart.manager.entity.EduClassEntity.class);
        verify(classMapper).insert(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getHeadTeacherId()).isEqualTo(teacherProfile.getId());
    }

    private EduTeacherProfileEntity teacherProfile(Long teacherProfileId) {
        EduTeacherProfileEntity teacherProfile = new EduTeacherProfileEntity();
        teacherProfile.setId(teacherProfileId);
        teacherProfile.setUserId(1001L);
        return teacherProfile;
    }
}
