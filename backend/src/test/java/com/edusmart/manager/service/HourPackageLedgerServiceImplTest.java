package com.edusmart.manager.service;

import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduClassSessionEntity;
import com.edusmart.manager.entity.EduHourDeductionRecordEntity;
import com.edusmart.manager.entity.EduOrderEntity;
import com.edusmart.manager.entity.EduOrderItemEntity;
import com.edusmart.manager.entity.EduStudentHourPackageEntity;
import com.edusmart.manager.mapper.EduClassSessionMapper;
import com.edusmart.manager.mapper.EduHourDeductionRecordMapper;
import com.edusmart.manager.mapper.EduOrderItemMapper;
import com.edusmart.manager.mapper.EduOrderMapper;
import com.edusmart.manager.mapper.EduStudentHourPackageMapper;
import com.edusmart.manager.service.impl.HourPackageLedgerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class HourPackageLedgerServiceImplTest {

    @Mock
    private EduOrderMapper orderMapper;
    @Mock
    private EduOrderItemMapper orderItemMapper;
    @Mock
    private EduStudentHourPackageMapper studentHourPackageMapper;
    @Mock
    private EduHourDeductionRecordMapper hourDeductionRecordMapper;
    @Mock
    private EduClassSessionMapper classSessionMapper;

    @InjectMocks
    private HourPackageLedgerServiceImpl service;

    @Test
    void issuePackagesForCompletedOrderShouldAggregateByCourseAndSkipIssuedCourses() {
        EduOrderEntity order = new EduOrderEntity();
        order.setId(101L);
        order.setStudentId(301L);
        order.setOrderType("HOUR_PACKAGE");
        order.setAmountTotal(new BigDecimal("350.00"));
        order.setAmountPaid(new BigDecimal("350.00"));
        order.setPayStatus("PAID");

        EduOrderItemEntity firstMathItem = new EduOrderItemEntity();
        firstMathItem.setOrderId(101L);
        firstMathItem.setCourseId(11L);
        firstMathItem.setHourCount(new BigDecimal("20.00"));

        EduOrderItemEntity secondMathItem = new EduOrderItemEntity();
        secondMathItem.setOrderId(101L);
        secondMathItem.setCourseId(11L);
        secondMathItem.setHourCount(new BigDecimal("5.00"));

        EduOrderItemEntity englishItem = new EduOrderItemEntity();
        englishItem.setOrderId(101L);
        englishItem.setCourseId(12L);
        englishItem.setHourCount(new BigDecimal("10.00"));

        EduStudentHourPackageEntity issuedEnglishPackage = new EduStudentHourPackageEntity();
        issuedEnglishPackage.setId(900L);
        issuedEnglishPackage.setOrderId(101L);
        issuedEnglishPackage.setCourseId(12L);

        when(orderMapper.selectById(101L)).thenReturn(order);
        when(orderItemMapper.selectList(any())).thenReturn(List.of(firstMathItem, secondMathItem, englishItem));
        when(studentHourPackageMapper.selectList(any())).thenReturn(List.of(issuedEnglishPackage));

        service.issuePackagesForCompletedOrder(101L, LocalDate.of(2026, 4, 12));

        ArgumentCaptor<EduStudentHourPackageEntity> packageCaptor = ArgumentCaptor.forClass(EduStudentHourPackageEntity.class);
        verify(studentHourPackageMapper).insert(packageCaptor.capture());
        EduStudentHourPackageEntity createdPackage = packageCaptor.getValue();
        assertThat(createdPackage.getStudentId()).isEqualTo(301L);
        assertThat(createdPackage.getOrderId()).isEqualTo(101L);
        assertThat(createdPackage.getCourseId()).isEqualTo(11L);
        assertThat(createdPackage.getTotalHours()).isEqualByComparingTo("25.00");
        assertThat(createdPackage.getUsedHours()).isEqualByComparingTo("0.00");
        assertThat(createdPackage.getRemainingHours()).isEqualByComparingTo("25.00");
        assertThat(createdPackage.getEffectiveDate()).isEqualTo(LocalDate.of(2026, 4, 12));
        assertThat(createdPackage.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void refreshAttendanceDeductionShouldSplitDeductionAcrossPackages() {
        EduAttendanceRecordEntity record = new EduAttendanceRecordEntity();
        record.setId(501L);
        record.setClassId(21L);
        record.setSessionId(31L);
        record.setStudentId(41L);
        record.setAttendanceDate(LocalDate.of(2026, 4, 12));
        record.setStatus("PRESENT");

        EduClassSessionEntity session = new EduClassSessionEntity();
        session.setId(31L);
        session.setCourseId(11L);
        session.setPlannedHours(new BigDecimal("3.50"));
        session.setSessionDate(LocalDate.of(2026, 4, 12));

        EduStudentHourPackageEntity firstPackage = createPackage(601L, 41L, 11L, "2.00", "0.00", "2.00");
        firstPackage.setEffectiveDate(LocalDate.of(2026, 4, 1));
        EduStudentHourPackageEntity secondPackage = createPackage(602L, 41L, 11L, "8.00", "1.00", "7.00");
        secondPackage.setEffectiveDate(LocalDate.of(2026, 4, 5));

        when(hourDeductionRecordMapper.selectList(any())).thenReturn(List.of());
        when(classSessionMapper.selectById(31L)).thenReturn(session);
        when(studentHourPackageMapper.selectList(any())).thenReturn(List.of(firstPackage, secondPackage));
        doAnswer(invocation -> {
            EduHourDeductionRecordEntity entity = invocation.getArgument(0);
            entity.setId(System.nanoTime());
            return 1;
        }).when(hourDeductionRecordMapper).insert(any(EduHourDeductionRecordEntity.class));

        service.refreshAttendanceDeduction(record);

        ArgumentCaptor<EduHourDeductionRecordEntity> deductionCaptor = ArgumentCaptor.forClass(EduHourDeductionRecordEntity.class);
        verify(hourDeductionRecordMapper, times(2)).insert(deductionCaptor.capture());
        List<EduHourDeductionRecordEntity> deductions = deductionCaptor.getAllValues();
        assertThat(deductions).extracting(EduHourDeductionRecordEntity::getHourPackageId).containsExactly(601L, 602L);
        assertThat(deductions).extracting(EduHourDeductionRecordEntity::getDeductHours)
                .containsExactly(new BigDecimal("2.00"), new BigDecimal("1.50"));
        assertThat(deductions).extracting(EduHourDeductionRecordEntity::getRemark)
                .containsOnly("ATTENDANCE_RECORD:501");

        ArgumentCaptor<EduStudentHourPackageEntity> packageCaptor = ArgumentCaptor.forClass(EduStudentHourPackageEntity.class);
        verify(studentHourPackageMapper, times(2)).updateById(packageCaptor.capture());
        List<EduStudentHourPackageEntity> updatedPackages = packageCaptor.getAllValues();
        assertThat(updatedPackages.get(0).getRemainingHours()).isEqualByComparingTo("0.00");
        assertThat(updatedPackages.get(0).getUsedHours()).isEqualByComparingTo("2.00");
        assertThat(updatedPackages.get(0).getStatus()).isEqualTo("CLOSED");
        assertThat(updatedPackages.get(1).getRemainingHours()).isEqualByComparingTo("5.50");
        assertThat(updatedPackages.get(1).getUsedHours()).isEqualByComparingTo("2.50");
        assertThat(updatedPackages.get(1).getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void refreshAttendanceDeductionShouldRejectWhenRemainingHoursAreInsufficient() {
        EduAttendanceRecordEntity record = new EduAttendanceRecordEntity();
        record.setId(502L);
        record.setClassId(21L);
        record.setSessionId(32L);
        record.setStudentId(41L);
        record.setAttendanceDate(LocalDate.of(2026, 4, 12));
        record.setStatus("PRESENT");

        EduClassSessionEntity session = new EduClassSessionEntity();
        session.setId(32L);
        session.setCourseId(11L);
        session.setPlannedHours(new BigDecimal("3.00"));
        session.setSessionDate(LocalDate.of(2026, 4, 12));

        EduStudentHourPackageEntity onlyPackage = createPackage(603L, 41L, 11L, "2.00", "0.00", "2.00");
        onlyPackage.setEffectiveDate(LocalDate.of(2026, 4, 1));

        when(hourDeductionRecordMapper.selectList(any())).thenReturn(List.of());
        when(classSessionMapper.selectById(32L)).thenReturn(session);
        when(studentHourPackageMapper.selectList(any())).thenReturn(List.of(onlyPackage));

        assertThatThrownBy(() -> service.refreshAttendanceDeduction(record))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(BAD_REQUEST);
    }

    private EduStudentHourPackageEntity createPackage(Long id,
                                                      Long studentId,
                                                      Long courseId,
                                                      String totalHours,
                                                      String usedHours,
                                                      String remainingHours) {
        EduStudentHourPackageEntity entity = new EduStudentHourPackageEntity();
        entity.setId(id);
        entity.setStudentId(studentId);
        entity.setCourseId(courseId);
        entity.setTotalHours(new BigDecimal(totalHours));
        entity.setUsedHours(new BigDecimal(usedHours));
        entity.setRemainingHours(new BigDecimal(remainingHours));
        entity.setStatus("ACTIVE");
        return entity;
    }
}
