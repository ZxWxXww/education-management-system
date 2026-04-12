package com.edusmart.manager.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public class AdminTeachingResourceOverviewDTO {
    private Long totalAssignments;
    private Long publishedThisWeek;
    private Long gradeRecords;
    private BigDecimal avgScore;
    private List<AdminTeachingRecentActivityDTO> recentActivities;

    public Long getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(Long totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public Long getPublishedThisWeek() {
        return publishedThisWeek;
    }

    public void setPublishedThisWeek(Long publishedThisWeek) {
        this.publishedThisWeek = publishedThisWeek;
    }

    public Long getGradeRecords() {
        return gradeRecords;
    }

    public void setGradeRecords(Long gradeRecords) {
        this.gradeRecords = gradeRecords;
    }

    public BigDecimal getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(BigDecimal avgScore) {
        this.avgScore = avgScore;
    }

    public List<AdminTeachingRecentActivityDTO> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<AdminTeachingRecentActivityDTO> recentActivities) {
        this.recentActivities = recentActivities;
    }
}
