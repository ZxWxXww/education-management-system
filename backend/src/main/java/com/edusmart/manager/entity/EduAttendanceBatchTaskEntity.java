package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;

@TableName("edu_attendance_batch_task")
public class EduAttendanceBatchTaskEntity extends BaseEntity {
    @TableField("operator_teacher_id")
    private Long operatorTeacherId;
    @TableField("class_id")
    private Long classId;
    @TableField("attendance_date")
    private LocalDate attendanceDate;
    @TableField("task_status")
    private String taskStatus;
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
