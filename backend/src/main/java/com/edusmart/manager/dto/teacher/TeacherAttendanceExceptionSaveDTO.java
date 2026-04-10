package com.edusmart.manager.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TeacherAttendanceExceptionSaveDTO {
    @NotNull(message = "考勤记录ID不能为空")
    private Long attendanceRecordId;
    @NotBlank(message = "异常类型不能为空")
    private String exceptionType;
    @NotBlank(message = "严重程度不能为空")
    private String severity;
    private Integer isResolved = 0;
    private Long resolvedBy;
    private String resolveRemark;

    public Long getAttendanceRecordId() { return attendanceRecordId; }
    public void setAttendanceRecordId(Long attendanceRecordId) { this.attendanceRecordId = attendanceRecordId; }
    public String getExceptionType() { return exceptionType; }
    public void setExceptionType(String exceptionType) { this.exceptionType = exceptionType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Integer getIsResolved() { return isResolved; }
    public void setIsResolved(Integer isResolved) { this.isResolved = isResolved; }
    public Long getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(Long resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getResolveRemark() { return resolveRemark; }
    public void setResolveRemark(String resolveRemark) { this.resolveRemark = resolveRemark; }
}
