package com.edusmart.manager.dto.admin;

import java.util.List;

public class AdminDisplayConfigDTO {
    private String siteTitle;
    private String siteSubtitle;
    private Boolean darkModeDefault;
    private String dashboardLayout;
    private Boolean showAttendanceWidget;
    private Boolean showFinanceWidget;
    private String loginNotice;
    private String brandColor;
    private String locale;
    private List<AdminDisplayPreviewItemDTO> previewRecords;

    public String getSiteTitle() {
        return siteTitle;
    }

    public void setSiteTitle(String siteTitle) {
        this.siteTitle = siteTitle;
    }

    public String getSiteSubtitle() {
        return siteSubtitle;
    }

    public void setSiteSubtitle(String siteSubtitle) {
        this.siteSubtitle = siteSubtitle;
    }

    public Boolean getDarkModeDefault() {
        return darkModeDefault;
    }

    public void setDarkModeDefault(Boolean darkModeDefault) {
        this.darkModeDefault = darkModeDefault;
    }

    public String getDashboardLayout() {
        return dashboardLayout;
    }

    public void setDashboardLayout(String dashboardLayout) {
        this.dashboardLayout = dashboardLayout;
    }

    public Boolean getShowAttendanceWidget() {
        return showAttendanceWidget;
    }

    public void setShowAttendanceWidget(Boolean showAttendanceWidget) {
        this.showAttendanceWidget = showAttendanceWidget;
    }

    public Boolean getShowFinanceWidget() {
        return showFinanceWidget;
    }

    public void setShowFinanceWidget(Boolean showFinanceWidget) {
        this.showFinanceWidget = showFinanceWidget;
    }

    public String getLoginNotice() {
        return loginNotice;
    }

    public void setLoginNotice(String loginNotice) {
        this.loginNotice = loginNotice;
    }

    public String getBrandColor() {
        return brandColor;
    }

    public void setBrandColor(String brandColor) {
        this.brandColor = brandColor;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<AdminDisplayPreviewItemDTO> getPreviewRecords() {
        return previewRecords;
    }

    public void setPreviewRecords(List<AdminDisplayPreviewItemDTO> previewRecords) {
        this.previewRecords = previewRecords;
    }
}
