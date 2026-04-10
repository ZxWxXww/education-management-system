package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.RoleSaveDTO;
import com.edusmart.manager.entity.EduRoleEntity;

import java.util.List;

public interface AdminRoleService {
    PageData<EduRoleEntity> page(long current, long size, String keyword);

    EduRoleEntity getById(Long id);

    Long create(RoleSaveDTO dto);

    void update(Long id, RoleSaveDTO dto);

    void delete(Long id);

    void assignPermissions(Long roleId, List<Long> permissionIds);
}
