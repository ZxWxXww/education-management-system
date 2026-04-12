package com.edusmart.manager.dto.admin;

import java.time.LocalDateTime;

public class AdminLogArchiveStrategyDTO {
    private Boolean autoArchiveEnabled;
    private Integer retentionDays;
    private LocalDateTime updatedAt;

    public Boolean getAutoArchiveEnabled() {
        return autoArchiveEnabled;
    }

    public void setAutoArchiveEnabled(Boolean autoArchiveEnabled) {
        this.autoArchiveEnabled = autoArchiveEnabled;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
