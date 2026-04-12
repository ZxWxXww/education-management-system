package com.edusmart.manager.service;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class HourPackageRefundReconcileTest {

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
    void reconcilePackagesForOrderShouldShrinkRemainingHoursAfterPartialRefund() {
        EduOrderEntity order = new EduOrderEntity();
        order.setId(201L);
        order.setStudentId(301L);
        order.setOrderType("HOUR_PACKAGE");
        order.setAmountTotal(new BigDecimal("300.00"));
        order.setAmountPaid(new BigDecimal("150.00"));
        order.setPayStatus("PARTIAL");

        EduOrderItemEntity item = new EduOrderItemEntity();
        item.setOrderId(201L);
        item.setCourseId(11L);
        item.setHourCount(new BigDecimal("6.00"));

        EduStudentHourPackageEntity hourPackage = new EduStudentHourPackageEntity();
        hourPackage.setId(801L);
        hourPackage.setOrderId(201L);
        hourPackage.setStudentId(301L);
        hourPackage.setCourseId(11L);
        hourPackage.setTotalHours(new BigDecimal("6.00"));
        hourPackage.setUsedHours(new BigDecimal("2.00"));
        hourPackage.setRemainingHours(new BigDecimal("4.00"));
        hourPackage.setEffectiveDate(LocalDate.of(2026, 4, 1));
        hourPackage.setStatus("ACTIVE");

        when(orderMapper.selectById(201L)).thenReturn(order);
        when(orderItemMapper.selectList(any())).thenReturn(List.of(item));
        when(studentHourPackageMapper.selectList(any())).thenReturn(List.of(hourPackage));

        service.reconcilePackagesForOrder(201L, LocalDate.of(2026, 4, 12));

        ArgumentCaptor<EduStudentHourPackageEntity> packageCaptor = ArgumentCaptor.forClass(EduStudentHourPackageEntity.class);
        verify(studentHourPackageMapper).updateById(packageCaptor.capture());
        EduStudentHourPackageEntity updated = packageCaptor.getValue();
        assertThat(updated.getUsedHours()).isEqualByComparingTo("2.00");
        assertThat(updated.getRemainingHours()).isEqualByComparingTo("1.00");
        assertThat(updated.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void reconcilePackagesForOrderShouldRejectWhenRefundWouldTakeBackUsedHours() {
        EduOrderEntity order = new EduOrderEntity();
        order.setId(202L);
        order.setStudentId(301L);
        order.setOrderType("HOUR_PACKAGE");
        order.setAmountTotal(new BigDecimal("300.00"));
        order.setAmountPaid(new BigDecimal("150.00"));
        order.setPayStatus("PARTIAL");

        EduOrderItemEntity item = new EduOrderItemEntity();
        item.setOrderId(202L);
        item.setCourseId(11L);
        item.setHourCount(new BigDecimal("6.00"));

        EduStudentHourPackageEntity hourPackage = new EduStudentHourPackageEntity();
        hourPackage.setId(802L);
        hourPackage.setOrderId(202L);
        hourPackage.setStudentId(301L);
        hourPackage.setCourseId(11L);
        hourPackage.setTotalHours(new BigDecimal("6.00"));
        hourPackage.setUsedHours(new BigDecimal("4.00"));
        hourPackage.setRemainingHours(new BigDecimal("2.00"));
        hourPackage.setEffectiveDate(LocalDate.of(2026, 4, 1));
        hourPackage.setStatus("ACTIVE");

        when(orderMapper.selectById(202L)).thenReturn(order);
        when(orderItemMapper.selectList(any())).thenReturn(List.of(item));
        when(studentHourPackageMapper.selectList(any())).thenReturn(List.of(hourPackage));

        assertThatThrownBy(() -> service.reconcilePackagesForOrder(202L, LocalDate.of(2026, 4, 12)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(BAD_REQUEST);
    }
}
