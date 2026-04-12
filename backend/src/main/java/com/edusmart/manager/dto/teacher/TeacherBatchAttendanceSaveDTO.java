package com.edusmart.manager.dto.teacher;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TeacherBatchAttendanceSaveDTO {
    private Long operatorTeacherId;
    @NotNull(message = "班级ID不能为空")
    private Long classId;
    @NotNull(message = "考勤日期不能为空")
    private LocalDate attendanceDate;
    private String taskStatus = "PENDING";
    private String remark;

    public Long getOperatorTeacherId() { return operatorTeacherId; }
    public void setOperatorTeacherId(Long operatorTeacherId) { this.operatorTeacherId = operatorTeacherId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
