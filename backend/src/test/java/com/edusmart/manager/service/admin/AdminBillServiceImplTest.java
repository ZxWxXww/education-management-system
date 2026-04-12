package com.edusmart.manager.service.admin;

import com.edusmart.manager.dto.admin.AdminBillPaymentSaveDTO;
import com.edusmart.manager.dto.admin.AdminBillRefundSaveDTO;
import com.edusmart.manager.dto.admin.BillPageQueryDTO;
import com.edusmart.manager.entity.EduBillEntity;
import com.edusmart.manager.entity.EduBillPaymentEntity;
import com.edusmart.manager.entity.EduOrderEntity;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.mapper.EduBillMapper;
import com.edusmart.manager.mapper.EduBillPaymentMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduOrderMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.HourPackageLedgerService;
import com.edusmart.manager.service.admin.impl.AdminBillServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class AdminBillServiceImplTest {

    @Mock
    private EduBillMapper billMapper;
    @Mock
    private EduUserMapper userMapper;
    @Mock
    private EduClassMapper classMapper;
    @Mock
    private EduStudentProfileMapper studentProfileMapper;
    @Mock
    private EduBillPaymentMapper billPaymentMapper;
    @Mock
    private EduOrderMapper orderMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private HourPackageLedgerService hourPackageLedgerService;

    @InjectMocks
    private AdminBillServiceImpl service;

    @Test
    void registerPaymentPersistsPaymentAndSyncsBillAndOrder() {
        EduBillEntity bill = new EduBillEntity();
        bill.setId(11L);
        bill.setOrderId(101L);
        bill.setStudentId(2L);
        bill.setBillType("HOUR_PACKAGE");
        bill.setAmount(new BigDecimal("1000.00"));
        bill.setPaidAmount(new BigDecimal("200.00"));
        bill.setStatus("PENDING");
        bill.setDueDate(LocalDate.now().plusDays(3));

        EduOrderEntity order = new EduOrderEntity();
        order.setId(101L);
        order.setAmountTotal(new BigDecimal("1000.00"));
        order.setAmountPaid(new BigDecimal("200.00"));
        order.setPayStatus("PARTIAL");

        when(billMapper.selectById(11L)).thenReturn(bill);
        when(billMapper.selectList(any())).thenReturn(List.of(bill));
        when(orderMapper.selectById(101L)).thenReturn(order);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doAnswer(invocation -> {
            EduBillPaymentEntity entity = invocation.getArgument(0);
            entity.setId(501L);
            return 1;
        }).when(billPaymentMapper).insert(any(EduBillPaymentEntity.class));

        AdminBillPaymentSaveDTO dto = new AdminBillPaymentSaveDTO();
        dto.setPayAmount(new BigDecimal("800.00"));
        dto.setPayChannel("ALIPAY");
        dto.setPayTime(LocalDateTime.of(2026, 4, 12, 14, 30, 0));

        Long paymentId = service.registerPayment(11L, dto);

        assertThat(paymentId).isEqualTo(501L);

        ArgumentCaptor<EduBillPaymentEntity> paymentCaptor = ArgumentCaptor.forClass(EduBillPaymentEntity.class);
        verify(billPaymentMapper).insert(paymentCaptor.capture());
        EduBillPaymentEntity payment = paymentCaptor.getValue();
        assertThat(payment.getBillId()).isEqualTo(11L);
        assertThat(payment.getPayNo()).startsWith("PY");
        assertThat(payment.getPayAmount()).isEqualByComparingTo("800.00");
        assertThat(payment.getPayChannel()).isEqualTo("ALIPAY");
        assertThat(payment.getPayTime()).isEqualTo(LocalDateTime.of(2026, 4, 12, 14, 30, 0));
        assertThat(payment.getOperatorUserId()).isEqualTo(1L);

        ArgumentCaptor<EduBillEntity> billCaptor = ArgumentCaptor.forClass(EduBillEntity.class);
        verify(billMapper).updateById(billCaptor.capture());
        EduBillEntity updatedBill = billCaptor.getValue();
        assertThat(updatedBill.getPaidAmount()).isEqualByComparingTo("1000.00");
        assertThat(updatedBill.getStatus()).isEqualTo("COMPLETED");

        ArgumentCaptor<EduOrderEntity> orderCaptor = ArgumentCaptor.forClass(EduOrderEntity.class);
        verify(orderMapper).updateById(orderCaptor.capture());
        EduOrderEntity updatedOrder = orderCaptor.getValue();
        assertThat(updatedOrder.getAmountPaid()).isEqualByComparingTo("1000.00");
        assertThat(updatedOrder.getPayStatus()).isEqualTo("PAID");
    }

    @Test
    void pageShouldExposeOrderIdForFinanceLinkage() {
        EduBillEntity bill = new EduBillEntity();
        bill.setId(21L);
        bill.setBillNo("BL-LINK-21");
        bill.setOrderId(301L);
        bill.setBillType("HOUR_PACKAGE");
        bill.setAmount(new BigDecimal("300.00"));
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStatus("PENDING");
        bill.setCreatedAt(LocalDateTime.of(2026, 4, 12, 10, 0, 0));
        bill.setUpdatedAt(LocalDateTime.of(2026, 4, 12, 10, 0, 0));

        Page<EduBillEntity> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(bill));
        when(billMapper.selectPage(any(Page.class), any())).thenReturn(page);

        BillPageQueryDTO queryDTO = new BillPageQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(10);

        var result = service.page(queryDTO);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getOrderId()).isEqualTo(301L);
    }

    @Test
    void registerPaymentRejectsAmountExceedingRemainingReceivable() {
        EduBillEntity bill = new EduBillEntity();
        bill.setId(11L);
        bill.setAmount(new BigDecimal("1000.00"));
        bill.setPaidAmount(new BigDecimal("900.00"));
        bill.setStatus("PENDING");
        when(billMapper.selectById(11L)).thenReturn(bill);

        AdminBillPaymentSaveDTO dto = new AdminBillPaymentSaveDTO();
        dto.setPayAmount(new BigDecimal("200.00"));
        dto.setPayChannel("CASH");
        dto.setPayTime(LocalDateTime.of(2026, 4, 12, 9, 0, 0));

        assertThatThrownBy(() -> service.registerPayment(11L, dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(BAD_REQUEST);
    }

    @Test
    void paymentListShouldUseDtoInsteadOfEntity() throws Exception {
        when(billPaymentMapper.selectList(any())).thenReturn(List.of());
        assertThat(service.listPayments(11L)).isEmpty();
    }

    @Test
    void registerPaymentShouldIssueHourPackagesWhenHourPackageOrderBecomesPaid() {
        EduBillEntity bill = new EduBillEntity();
        bill.setId(11L);
        bill.setOrderId(101L);
        bill.setStudentId(2L);
        bill.setBillType("HOUR_PACKAGE");
        bill.setAmount(new BigDecimal("300.00"));
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStatus("PENDING");
        bill.setDueDate(LocalDate.now().plusDays(3));

        EduOrderEntity order = new EduOrderEntity();
        order.setId(101L);
        order.setOrderType("HOUR_PACKAGE");
        order.setAmountTotal(new BigDecimal("300.00"));
        order.setAmountPaid(BigDecimal.ZERO);
        order.setPayStatus("UNPAID");

        when(billMapper.selectById(11L)).thenReturn(bill);
        when(billMapper.selectList(any())).thenReturn(List.of(bill));
        when(orderMapper.selectById(101L)).thenReturn(order);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doAnswer(invocation -> {
            EduBillPaymentEntity entity = invocation.getArgument(0);
            entity.setId(888L);
            return 1;
        }).when(billPaymentMapper).insert(any(EduBillPaymentEntity.class));

        AdminBillPaymentSaveDTO dto = new AdminBillPaymentSaveDTO();
        dto.setPayAmount(new BigDecimal("300.00"));
        dto.setPayChannel("ALIPAY");
        dto.setPayTime(LocalDateTime.of(2026, 4, 12, 16, 0, 0));

        service.registerPayment(11L, dto);

        verify(hourPackageLedgerService).reconcilePackagesForOrder(101L, LocalDate.of(2026, 4, 12));
    }

    @Test
    void registerPaymentShouldNormalizeLegacyBankChannelBeforePersisting() {
        EduBillEntity bill = new EduBillEntity();
        bill.setId(13L);
        bill.setOrderId(103L);
        bill.setStudentId(2L);
        bill.setBillType("HOUR_PACKAGE");
        bill.setAmount(new BigDecimal("300.00"));
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStatus("PENDING");
        bill.setDueDate(LocalDate.now().plusDays(3));

        EduOrderEntity order = new EduOrderEntity();
        order.setId(103L);
        order.setOrderType("HOUR_PACKAGE");
        order.setAmountTotal(new BigDecimal("300.00"));
        order.setAmountPaid(BigDecimal.ZERO);
        order.setPayStatus("UNPAID");

        when(billMapper.selectById(13L)).thenReturn(bill);
        when(billMapper.selectList(any())).thenReturn(List.of(bill));
        when(orderMapper.selectById(103L)).thenReturn(order);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doAnswer(invocation -> {
            EduBillPaymentEntity entity = invocation.getArgument(0);
            entity.setId(903L);
            return 1;
        }).when(billPaymentMapper).insert(any(EduBillPaymentEntity.class));

        AdminBillPaymentSaveDTO dto = new AdminBillPaymentSaveDTO();
        dto.setPayAmount(new BigDecimal("300.00"));
        dto.setPayChannel("BANK");
        dto.setPayTime(LocalDateTime.of(2026, 4, 12, 18, 0, 0));

        service.registerPayment(13L, dto);

        ArgumentCaptor<EduBillPaymentEntity> paymentCaptor = ArgumentCaptor.forClass(EduBillPaymentEntity.class);
        verify(billPaymentMapper).insert(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getPayChannel()).isEqualTo("BANK_TRANSFER");
    }

    @Test
    void registerRefundShouldRejectUnsupportedPayChannelBeforeReachingDatabase() {
        EduBillEntity bill = new EduBillEntity();
        bill.setId(14L);
        bill.setOrderId(104L);
        bill.setStudentId(2L);
        bill.setBillType("HOUR_PACKAGE");
        bill.setAmount(new BigDecimal("300.00"));
        bill.setPaidAmount(new BigDecimal("300.00"));
        bill.setStatus("COMPLETED");

        when(billMapper.selectById(14L)).thenReturn(bill);

        AdminBillRefundSaveDTO dto = new AdminBillRefundSaveDTO();
        dto.setRefundAmount(new BigDecimal("100.00"));
        dto.setPayChannel("UNKNOWN_CHANNEL");
        dto.setPayTime(LocalDateTime.of(2026, 4, 13, 11, 0, 0));
        dto.setActionType("REFUND");

        assertThatThrownBy(() -> service.registerRefund(14L, dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(BAD_REQUEST);
    }

    @Test
    void registerRefundShouldPersistNegativePaymentAndVoidBillOnFullRefund() {
        EduBillEntity bill = new EduBillEntity();
        bill.setId(11L);
        bill.setOrderId(101L);
        bill.setStudentId(2L);
        bill.setBillType("HOUR_PACKAGE");
        bill.setAmount(new BigDecimal("300.00"));
        bill.setPaidAmount(new BigDecimal("300.00"));
        bill.setStatus("COMPLETED");

        EduOrderEntity order = new EduOrderEntity();
        order.setId(101L);
        order.setOrderType("HOUR_PACKAGE");
        order.setAmountTotal(new BigDecimal("300.00"));
        order.setAmountPaid(new BigDecimal("300.00"));
        order.setPayStatus("PAID");

        when(billMapper.selectById(11L)).thenReturn(bill);
        when(billMapper.selectList(any())).thenReturn(List.of(bill));
        when(orderMapper.selectById(101L)).thenReturn(order);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doAnswer(invocation -> {
            EduBillPaymentEntity entity = invocation.getArgument(0);
            entity.setId(901L);
            return 1;
        }).when(billPaymentMapper).insert(any(EduBillPaymentEntity.class));

        AdminBillRefundSaveDTO dto = new AdminBillRefundSaveDTO();
        dto.setRefundAmount(new BigDecimal("300.00"));
        dto.setPayChannel("ALIPAY");
        dto.setPayTime(LocalDateTime.of(2026, 4, 13, 9, 0, 0));
        dto.setActionType("REFUND");

        Long refundId = service.registerRefund(11L, dto);

        assertThat(refundId).isEqualTo(901L);

        ArgumentCaptor<EduBillPaymentEntity> paymentCaptor = ArgumentCaptor.forClass(EduBillPaymentEntity.class);
        verify(billPaymentMapper).insert(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getPayNo()).startsWith("RF");
        assertThat(paymentCaptor.getValue().getPayAmount()).isEqualByComparingTo("-300.00");

        ArgumentCaptor<EduBillEntity> billCaptor = ArgumentCaptor.forClass(EduBillEntity.class);
        verify(billMapper).updateById(billCaptor.capture());
        assertThat(billCaptor.getValue().getPaidAmount()).isEqualByComparingTo("0.00");
        assertThat(billCaptor.getValue().getStatus()).isEqualTo("VOID");

        ArgumentCaptor<EduOrderEntity> orderCaptor = ArgumentCaptor.forClass(EduOrderEntity.class);
        verify(orderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getAmountPaid()).isEqualByComparingTo("0.00");
        assertThat(orderCaptor.getValue().getPayStatus()).isEqualTo("REFUNDED");

        verify(hourPackageLedgerService).reconcilePackagesForOrder(101L, LocalDate.of(2026, 4, 13));
    }

    @Test
    void registerRefundShouldReopenBillOnFullUnsettle() {
        EduBillEntity bill = new EduBillEntity();
        bill.setId(12L);
        bill.setOrderId(102L);
        bill.setStudentId(2L);
        bill.setBillType("HOUR_PACKAGE");
        bill.setAmount(new BigDecimal("300.00"));
        bill.setPaidAmount(new BigDecimal("300.00"));
        bill.setStatus("COMPLETED");

        EduOrderEntity order = new EduOrderEntity();
        order.setId(102L);
        order.setOrderType("HOUR_PACKAGE");
        order.setAmountTotal(new BigDecimal("300.00"));
        order.setAmountPaid(new BigDecimal("300.00"));
        order.setPayStatus("PAID");

        when(billMapper.selectById(12L)).thenReturn(bill);
        when(billMapper.selectList(any())).thenReturn(List.of(bill));
        when(orderMapper.selectById(102L)).thenReturn(order);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doAnswer(invocation -> {
            EduBillPaymentEntity entity = invocation.getArgument(0);
            entity.setId(902L);
            return 1;
        }).when(billPaymentMapper).insert(any(EduBillPaymentEntity.class));

        AdminBillRefundSaveDTO dto = new AdminBillRefundSaveDTO();
        dto.setRefundAmount(new BigDecimal("300.00"));
        dto.setPayChannel("ALIPAY");
        dto.setPayTime(LocalDateTime.of(2026, 4, 13, 10, 0, 0));
        dto.setActionType("UNSETTLE");

        service.registerRefund(12L, dto);

        ArgumentCaptor<EduBillEntity> billCaptor = ArgumentCaptor.forClass(EduBillEntity.class);
        verify(billMapper).updateById(billCaptor.capture());
        assertThat(billCaptor.getValue().getPaidAmount()).isEqualByComparingTo("0.00");
        assertThat(billCaptor.getValue().getStatus()).isEqualTo("PENDING");

        ArgumentCaptor<EduOrderEntity> orderCaptor = ArgumentCaptor.forClass(EduOrderEntity.class);
        verify(orderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getAmountPaid()).isEqualByComparingTo("0.00");
        assertThat(orderCaptor.getValue().getPayStatus()).isEqualTo("UNPAID");
    }
}
