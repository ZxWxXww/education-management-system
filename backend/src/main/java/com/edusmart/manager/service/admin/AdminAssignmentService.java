package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminAssignmentPageItemDTO;
import com.edusmart.manager.dto.admin.AdminAssignmentPageQueryDTO;

public interface AdminAssignmentService {
    PageData<AdminAssignmentPageItemDTO> page(AdminAssignmentPageQueryDTO queryDTO);
}
