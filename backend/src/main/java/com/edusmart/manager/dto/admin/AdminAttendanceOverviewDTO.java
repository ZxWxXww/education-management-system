package com.edusmart.manager.dto.admin;

import java.util.List;

public class AdminAttendanceOverviewDTO {
    private Long totalAttendanceCount;
    private Double punctualRate;
    private Long leaveCount;
    private Long exceptionCount;
    private List<AdminAttendanceTrendPointDTO> trend;
    private List<AdminAttendanceClassOverviewDTO> classOverview;
    private List<AdminAttendanceFocusAlertDTO> focusAlerts;

    public Long getTotalAttendanceCount() {
        return totalAttendanceCount;
    }

    public void setTotalAttendanceCount(Long totalAttendanceCount) {
        this.totalAttendanceCount = totalAttendanceCount;
    }

    public Double getPunctualRate() {
        return punctualRate;
    }

    public void setPunctualRate(Double punctualRate) {
        this.punctualRate = punctualRate;
    }

    public Long getLeaveCount() {
        return leaveCount;
    }

    public void setLeaveCount(Long leaveCount) {
        this.leaveCount = leaveCount;
    }

    public Long getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(Long exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    public List<AdminAttendanceTrendPointDTO> getTrend() {
        return trend;
    }

    public void setTrend(List<AdminAttendanceTrendPointDTO> trend) {
        this.trend = trend;
    }

    public List<AdminAttendanceClassOverviewDTO> getClassOverview() {
        return classOverview;
    }

    public void setClassOverview(List<AdminAttendanceClassOverviewDTO> classOverview) {
        this.classOverview = classOverview;
    }

    public List<AdminAttendanceFocusAlertDTO> getFocusAlerts() {
        return focusAlerts;
    }

    public void setFocusAlerts(List<AdminAttendanceFocusAlertDTO> focusAlerts) {
        this.focusAlerts = focusAlerts;
    }
}
