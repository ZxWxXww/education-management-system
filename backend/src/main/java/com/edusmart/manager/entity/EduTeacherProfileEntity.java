package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;

@TableName("edu_teacher_profile")
public class EduTeacherProfileEntity extends BaseEntity {
    @TableField("user_id")
    private Long userId;
    @TableField("teacher_no")
    private String teacherNo;
    private String title;
    private String subject;
    private String intro;
    @TableField("hire_date")
    private LocalDate hireDate;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTeacherNo() { return teacherNo; }
    public void setTeacherNo(String teacherNo) { this.teacherNo = teacherNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
