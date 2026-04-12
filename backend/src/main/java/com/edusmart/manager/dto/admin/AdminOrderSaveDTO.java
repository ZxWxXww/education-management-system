package com.edusmart.manager.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public class AdminOrderSaveDTO {
    @NotNull(message = "学员 ID 不能为空")
    private Long studentId;

    @Pattern(regexp = "HOUR_PACKAGE|MATERIAL|EXAM_FEE|OTHER", message = "订单类型不合法")
    private String orderType;

    private LocalDate dueDate;
    private String remark;

    @Valid
    @NotEmpty(message = "订单明细不能为空")
    private List<AdminOrderItemSaveDTO> items;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<AdminOrderItemSaveDTO> getItems() {
        return items;
    }

    public void setItems(List<AdminOrderItemSaveDTO> items) {
        this.items = items;
    }
}
