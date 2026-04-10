package com.edusmart.manager.dto.student;

import com.edusmart.manager.dto.BasePageQueryDTO;

import java.time.LocalDate;

public class StudentAttendancePageQueryDTO extends BasePageQueryDTO {
    private Long studentId;
    private Long classId;
    private String status;
    private LocalDate attendanceDate;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
}
