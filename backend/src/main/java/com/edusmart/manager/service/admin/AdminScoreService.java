package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminScorePageItemDTO;
import com.edusmart.manager.dto.admin.AdminScorePageQueryDTO;

public interface AdminScoreService {
    PageData<AdminScorePageItemDTO> page(AdminScorePageQueryDTO queryDTO);
}
