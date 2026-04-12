package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminUserPageItemDTO;
import com.edusmart.manager.dto.admin.AdminUserPageQueryDTO;
import com.edusmart.manager.dto.admin.AdminUserSaveDTO;

import java.util.List;

public interface AdminUserService {
    PageData<AdminUserPageItemDTO> page(AdminUserPageQueryDTO queryDTO);

    AdminUserPageItemDTO getById(Long id);

    Long create(AdminUserSaveDTO dto);

    void update(Long id, AdminUserSaveDTO dto);

    void delete(Long id);

    void assignRoles(Long userId, List<Long> roleIds);
}
