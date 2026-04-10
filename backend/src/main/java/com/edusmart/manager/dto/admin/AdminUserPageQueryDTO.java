package com.edusmart.manager.dto.admin;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class AdminUserPageQueryDTO extends BasePageQueryDTO {
    private String keyword;
    private Integer status;
    private Long roleId;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
