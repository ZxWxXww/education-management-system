package com.edusmart.manager.service.student;

import com.edusmart.manager.dto.student.StudentHourPackageSummaryDTO;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduHourDeductionRecordEntity;
import com.edusmart.manager.entity.EduStudentHourPackageEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.mapper.EduBillMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduHourDeductionRecordMapper;
import com.edusmart.manager.mapper.EduStudentHourPackageMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.student.impl.StudentBillServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentBillServiceImplTest {

    @Mock
    private EduBillMapper billMapper;
    @Mock
    private EduClassMapper classMapper;
    @Mock
    private EduStudentProfileMapper studentProfileMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private EduStudentHourPackageMapper studentHourPackageMapper;
    @Mock
    private EduHourDeductionRecordMapper hourDeductionRecordMapper;
    @Mock
    private EduCourseMapper courseMapper;

    @InjectMocks
    private StudentBillServiceImpl service;

    @Test
    void getHourPackageSummaryShouldOnlyUseCurrentStudentData() {
        EduStudentProfileEntity currentProfile = new EduStudentProfileEntity();
        currentProfile.setId(301L);
        currentProfile.setUserId(91L);

        EduStudentHourPackageEntity activePackage = new EduStudentHourPackageEntity();
        activePackage.setId(11L);
        activePackage.setStudentId(301L);
        activePackage.setCourseId(501L);
        activePackage.setOrderId(801L);
        activePackage.setTotalHours(new BigDecimal("40.00"));
        activePackage.setUsedHours(new BigDecimal("10.00"));
        activePackage.setRemainingHours(new BigDecimal("30.00"));
        activePackage.setEffectiveDate(LocalDate.of(2026, 4, 1));
        activePackage.setStatus("ACTIVE");

        EduHourDeductionRecordEntity deduction = new EduHourDeductionRecordEntity();
        deduction.setId(21L);
        deduction.setHourPackageId(11L);
        deduction.setStudentId(301L);
        deduction.setClassId(701L);
        deduction.setSessionId(901L);
        deduction.setDeductHours(new BigDecimal("2.00"));
        deduction.setDeductType("CLASS_ATTEND");
        deduction.setBizDate(LocalDate.of(2026, 4, 12));

        com.edusmart.manager.entity.EduCourseEntity course = new com.edusmart.manager.entity.EduCourseEntity();
        course.setId(501L);
        course.setCourseName("数学培优");

        EduClassEntity clazz = new EduClassEntity();
        clazz.setId(701L);
        clazz.setClassName("高一数学 A 班");

        when(currentUserService.getCurrentUserId()).thenReturn(91L);
        when(studentProfileMapper.selectOne(any())).thenReturn(currentProfile);
        when(studentHourPackageMapper.selectList(any())).thenReturn(List.of(activePackage));
        when(hourDeductionRecordMapper.selectList(any())).thenReturn(List.of(deduction));
        when(courseMapper.selectBatchIds(any())).thenReturn(List.of(course));
        when(classMapper.selectBatchIds(any())).thenReturn(List.of(clazz));

        StudentHourPackageSummaryDTO summary = service.getHourPackageSummary();

        assertThat(summary.getTotalRemainingHours()).isEqualByComparingTo("30.00");
        assertThat(summary.getActivePackageCount()).isEqualTo(1);
        assertThat(summary.getPackages()).hasSize(1);
        assertThat(summary.getPackages().get(0).getCourseName()).isEqualTo("数学培优");
        assertThat(summary.getDeductions()).hasSize(1);
        assertThat(summary.getDeductions().get(0).getClassName()).isEqualTo("高一数学 A 班");
        assertThat(summary.getDeductions().get(0).getDeductHours()).isEqualByComparingTo("2.00");
    }
}
