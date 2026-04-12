package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminOrderDetailDTO;
import com.edusmart.manager.dto.admin.AdminOrderPageItemDTO;
import com.edusmart.manager.dto.admin.AdminOrderPageQueryDTO;
import com.edusmart.manager.dto.admin.AdminOrderSaveDTO;

public interface AdminOrderService {
    PageData<AdminOrderPageItemDTO> page(AdminOrderPageQueryDTO queryDTO);

    AdminOrderDetailDTO getById(Long id);

    Long create(AdminOrderSaveDTO dto);
}
