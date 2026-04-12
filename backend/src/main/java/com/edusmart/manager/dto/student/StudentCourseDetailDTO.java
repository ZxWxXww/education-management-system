package com.edusmart.manager.dto.student;

import java.math.BigDecimal;
import java.util.List;

public class StudentCourseDetailDTO extends StudentCoursePageItemDTO {
    private Integer studentCount;
    private BigDecimal averageAttendanceRate;
    private BigDecimal averageScore;
    private List<StudentCourseScheduleItemDTO> scheduleList;
    private List<StudentCourseClassmateItemDTO> classmates;

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
    public BigDecimal getAverageAttendanceRate() { return averageAttendanceRate; }
    public void setAverageAttendanceRate(BigDecimal averageAttendanceRate) { this.averageAttendanceRate = averageAttendanceRate; }
    public BigDecimal getAverageScore() { return averageScore; }
    public void setAverageScore(BigDecimal averageScore) { this.averageScore = averageScore; }
    public List<StudentCourseScheduleItemDTO> getScheduleList() { return scheduleList; }
    public void setScheduleList(List<StudentCourseScheduleItemDTO> scheduleList) { this.scheduleList = scheduleList; }
    public List<StudentCourseClassmateItemDTO> getClassmates() { return classmates; }
    public void setClassmates(List<StudentCourseClassmateItemDTO> classmates) { this.classmates = classmates; }
}
