package com.edusmart.manager.service.admin;

import com.edusmart.manager.dto.admin.AdminDisplayConfigDTO;
import com.edusmart.manager.dto.admin.AdminDisplaySaveDTO;

public interface AdminDisplayService {
    AdminDisplayConfigDTO getCurrent();
    void save(AdminDisplaySaveDTO dto);
    AdminDisplayConfigDTO reset();
}
