package com.edusmart.manager.dto.admin;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class RolePermissionAssignDTO {
    @NotEmpty(message = "权限ID列表不能为空")
    private List<Long> permissionIds;

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
