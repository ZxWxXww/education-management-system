package com.edusmart.manager.dto.student;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StudentCourseClassmateItemDTO {
    private Long studentProfileId;
    private String studentNo;
    private String studentName;
    private BigDecimal attendanceRate;
    private BigDecimal latestScore;
    private LocalDateTime updatedAt;

    public Long getStudentProfileId() { return studentProfileId; }
    public void setStudentProfileId(Long studentProfileId) { this.studentProfileId = studentProfileId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public BigDecimal getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(BigDecimal attendanceRate) { this.attendanceRate = attendanceRate; }
    public BigDecimal getLatestScore() { return latestScore; }
    public void setLatestScore(BigDecimal latestScore) { this.latestScore = latestScore; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
