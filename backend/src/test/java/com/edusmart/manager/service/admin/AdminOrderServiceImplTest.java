package com.edusmart.manager.service.admin;

import com.edusmart.manager.dto.admin.AdminOrderItemSaveDTO;
import com.edusmart.manager.dto.admin.AdminOrderSaveDTO;
import com.edusmart.manager.entity.EduBillEntity;
import com.edusmart.manager.entity.EduOrderEntity;
import com.edusmart.manager.entity.EduOrderItemEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.mapper.EduBillMapper;
import com.edusmart.manager.mapper.EduOrderItemMapper;
import com.edusmart.manager.mapper.EduOrderMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.service.admin.impl.AdminOrderServiceImpl;
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
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceImplTest {

    @Mock
    private EduOrderMapper orderMapper;
    @Mock
    private EduOrderItemMapper orderItemMapper;
    @Mock
    private EduBillMapper billMapper;
    @Mock
    private EduStudentProfileMapper studentProfileMapper;

    @InjectMocks
    private AdminOrderServiceImpl service;

    @Test
    void createPersistsOrderItemsAndLinkedBillWithServerCalculatedAmounts() {
        EduStudentProfileEntity studentProfile = new EduStudentProfileEntity();
        studentProfile.setId(2L);
        studentProfile.setUserId(3L);
        when(studentProfileMapper.selectById(2L)).thenReturn(studentProfile);

        doAnswer(invocation -> {
            EduOrderEntity entity = invocation.getArgument(0);
            entity.setId(101L);
            return 1;
        }).when(orderMapper).insert(any(EduOrderEntity.class));

        AdminOrderSaveDTO dto = new AdminOrderSaveDTO();
        dto.setStudentId(2L);
        dto.setOrderType("HOUR_PACKAGE");
        dto.setDueDate(LocalDate.of(2026, 4, 30));
        dto.setRemark("4 月联调订单");
        dto.setItems(List.of(
                item("高阶数学 20 课时包", 2, "500.00", "20.00"),
                item("教材资料", 3, "100.00", null)
        ));

        Long orderId = service.create(dto);

        assertThat(orderId).isEqualTo(101L);

        ArgumentCaptor<EduOrderEntity> orderCaptor = ArgumentCaptor.forClass(EduOrderEntity.class);
        verify(orderMapper).insert(orderCaptor.capture());
        EduOrderEntity order = orderCaptor.getValue();
        assertThat(order.getOrderNo()).startsWith("OD");
        assertThat(order.getStudentId()).isEqualTo(2L);
        assertThat(order.getOrderType()).isEqualTo("HOUR_PACKAGE");
        assertThat(order.getAmountTotal()).isEqualByComparingTo("1300.00");
        assertThat(order.getAmountPaid()).isEqualByComparingTo("0.00");
        assertThat(order.getPayStatus()).isEqualTo("UNPAID");
        assertThat(order.getRemark()).isEqualTo("4 月联调订单");

        ArgumentCaptor<EduOrderItemEntity> orderItemCaptor = ArgumentCaptor.forClass(EduOrderItemEntity.class);
        verify(orderItemMapper, times(2)).insert(orderItemCaptor.capture());
        List<EduOrderItemEntity> items = orderItemCaptor.getAllValues();
        assertThat(items).hasSize(2);
        assertThat(items).allMatch(item -> Long.valueOf(101L).equals(item.getOrderId()));
        assertThat(items.get(0).getLineAmount()).isEqualByComparingTo("1000.00");
        assertThat(items.get(1).getLineAmount()).isEqualByComparingTo("300.00");

        ArgumentCaptor<EduBillEntity> billCaptor = ArgumentCaptor.forClass(EduBillEntity.class);
        verify(billMapper).insert(billCaptor.capture());
        EduBillEntity bill = billCaptor.getValue();
        assertThat(bill.getBillNo()).startsWith("BL");
        assertThat(bill.getOrderId()).isEqualTo(101L);
        assertThat(bill.getStudentId()).isEqualTo(2L);
        assertThat(bill.getBillType()).isEqualTo("HOUR_PACKAGE");
        assertThat(bill.getAmount()).isEqualByComparingTo("1300.00");
        assertThat(bill.getPaidAmount()).isEqualByComparingTo("0.00");
        assertThat(bill.getStatus()).isEqualTo("PENDING");
        assertThat(bill.getDueDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(bill.getRemark()).isEqualTo("4 月联调订单");
    }

    @Test
    void createRejectsUnknownStudentProfile() {
        when(studentProfileMapper.selectById(999L)).thenReturn(null);

        AdminOrderSaveDTO dto = new AdminOrderSaveDTO();
        dto.setStudentId(999L);
        dto.setOrderType("HOUR_PACKAGE");
        dto.setItems(List.of(item("高阶数学 20 课时包", 1, "980.00", "20.00")));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(NOT_FOUND);
    }

    private AdminOrderItemSaveDTO item(String itemName, int quantity, String unitPrice, String hourCount) {
        AdminOrderItemSaveDTO item = new AdminOrderItemSaveDTO();
        item.setItemName(itemName);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        if (hourCount != null) {
            item.setHourCount(new BigDecimal(hourCount));
        }
        return item;
    }
}
