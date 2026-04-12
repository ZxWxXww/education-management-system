package com.edusmart.manager.dto.admin;

import java.util.List;

public class AdminOrderDetailDTO extends AdminOrderPageItemDTO {
    private List<AdminOrderDetailItemDTO> items;

    public List<AdminOrderDetailItemDTO> getItems() {
        return items;
    }

    public void setItems(List<AdminOrderDetailItemDTO> items) {
        this.items = items;
    }
}
