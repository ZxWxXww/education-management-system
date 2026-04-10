package com.edusmart.manager.dto.teacher;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class TeacherAttendanceExceptionPageQueryDTO extends BasePageQueryDTO {
    private Long attendanceRecordId;
    private Integer isResolved;
    private String exceptionType;

    public Long getAttendanceRecordId() { return attendanceRecordId; }
    public void setAttendanceRecordId(Long attendanceRecordId) { this.attendanceRecordId = attendanceRecordId; }
    public Integer getIsResolved() { return isResolved; }
    public void setIsResolved(Integer isResolved) { this.isResolved = isResolved; }
    public String getExceptionType() { return exceptionType; }
    public void setExceptionType(String exceptionType) { this.exceptionType = exceptionType; }
}
