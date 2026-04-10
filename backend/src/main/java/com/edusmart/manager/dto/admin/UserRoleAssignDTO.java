package com.edusmart.manager.dto.admin;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class UserRoleAssignDTO {
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
