package com.edusmart.manager.dto.student;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class StudentAttendanceExceptionPageQueryDTO extends BasePageQueryDTO {
    private Integer isResolved;
    private String exceptionType;
    private Long attendanceRecordId;

    public Integer getIsResolved() { return isResolved; }
    public void setIsResolved(Integer isResolved) { this.isResolved = isResolved; }
    public String getExceptionType() { return exceptionType; }
    public void setExceptionType(String exceptionType) { this.exceptionType = exceptionType; }
    public Long getAttendanceRecordId() { return attendanceRecordId; }
    public void setAttendanceRecordId(Long attendanceRecordId) { this.attendanceRecordId = attendanceRecordId; }
}
