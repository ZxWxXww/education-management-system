package com.edusmart.manager.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TeacherAttendanceSaveDTO {
    @NotNull(message = "班级ID不能为空")
    private Long classId;
    private Long sessionId;
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;
    @NotNull(message = "考勤日期不能为空")
    private LocalDate attendanceDate;
    @NotBlank(message = "考勤状态不能为空")
    private String status;
    private String remark;

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
