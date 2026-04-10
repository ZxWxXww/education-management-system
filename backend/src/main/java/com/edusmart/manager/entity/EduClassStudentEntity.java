package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;

@TableName("edu_class_student")
public class EduClassStudentEntity extends BaseEntity {
    @TableField("class_id")
    private Long classId;
    @TableField("student_id")
    private Long studentId;
    @TableField("join_date")
    private LocalDate joinDate;
    @TableField("leave_date")
    private LocalDate leaveDate;
    private Integer status;

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }
    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
