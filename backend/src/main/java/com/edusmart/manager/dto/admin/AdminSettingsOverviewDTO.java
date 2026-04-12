package com.edusmart.manager.dto.admin;

import java.util.List;

public class AdminSettingsOverviewDTO {
    private Long systemSettingCount;
    private Long logStrategyCount;
    private Long displayTemplateCount;
    private Long pendingApprovalCount;
    private List<AdminSettingsModuleStatusDTO> moduleStatusList;

    public Long getSystemSettingCount() {
        return systemSettingCount;
    }

    public void setSystemSettingCount(Long systemSettingCount) {
        this.systemSettingCount = systemSettingCount;
    }

    public Long getLogStrategyCount() {
        return logStrategyCount;
    }

    public void setLogStrategyCount(Long logStrategyCount) {
        this.logStrategyCount = logStrategyCount;
    }

    public Long getDisplayTemplateCount() {
        return displayTemplateCount;
    }

    public void setDisplayTemplateCount(Long displayTemplateCount) {
        this.displayTemplateCount = displayTemplateCount;
    }

    public Long getPendingApprovalCount() {
        return pendingApprovalCount;
    }

    public void setPendingApprovalCount(Long pendingApprovalCount) {
        this.pendingApprovalCount = pendingApprovalCount;
    }

    public List<AdminSettingsModuleStatusDTO> getModuleStatusList() {
        return moduleStatusList;
    }

    public void setModuleStatusList(List<AdminSettingsModuleStatusDTO> moduleStatusList) {
        this.moduleStatusList = moduleStatusList;
    }
}
