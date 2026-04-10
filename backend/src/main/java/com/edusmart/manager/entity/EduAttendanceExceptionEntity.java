package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("edu_attendance_exception")
public class EduAttendanceExceptionEntity extends BaseEntity {
    @TableField("attendance_record_id")
    private Long attendanceRecordId;

    @TableField("exception_type")
    private String exceptionType;

    private String severity;

    @TableField("is_resolved")
    private Integer isResolved;

    @TableField("resolved_by")
    private Long resolvedBy;

    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    @TableField("resolve_remark")
    private String resolveRemark;

    public Long getAttendanceRecordId() {
        return attendanceRecordId;
    }

    public void setAttendanceRecordId(Long attendanceRecordId) {
        this.attendanceRecordId = attendanceRecordId;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Integer getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Integer isResolved) {
        this.isResolved = isResolved;
    }

    public Long getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Long resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolveRemark() {
        return resolveRemark;
    }

    public void setResolveRemark(String resolveRemark) {
        this.resolveRemark = resolveRemark;
    }
}
