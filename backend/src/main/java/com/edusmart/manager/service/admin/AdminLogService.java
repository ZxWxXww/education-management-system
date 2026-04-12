package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminLogArchiveStrategyDTO;
import com.edusmart.manager.dto.admin.AdminLogPageItemDTO;
import com.edusmart.manager.dto.admin.AdminLogPageQueryDTO;

public interface AdminLogService {
    PageData<AdminLogPageItemDTO> page(AdminLogPageQueryDTO queryDTO);
    AdminLogArchiveStrategyDTO getArchiveStrategy();
    void saveArchiveStrategy(AdminLogArchiveStrategyDTO dto);
    byte[] exportCsv(AdminLogPageQueryDTO queryDTO);
}
