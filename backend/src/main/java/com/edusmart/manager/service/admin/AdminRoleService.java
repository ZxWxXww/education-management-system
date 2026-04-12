package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminRolePageItemDTO;
import com.edusmart.manager.dto.admin.RoleSaveDTO;

import java.util.List;

public interface AdminRoleService {
    PageData<AdminRolePageItemDTO> page(long current, long size, String keyword);

    AdminRolePageItemDTO getById(Long id);

    Long create(RoleSaveDTO dto);

    void update(Long id, RoleSaveDTO dto);

    void delete(Long id);

    void assignPermissions(Long roleId, List<Long> permissionIds);
}
