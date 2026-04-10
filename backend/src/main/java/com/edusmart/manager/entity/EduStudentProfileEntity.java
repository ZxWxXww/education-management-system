package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;

@TableName("edu_student_profile")
public class EduStudentProfileEntity extends BaseEntity {
    @TableField("user_id")
    private Long userId;
    @TableField("student_no")
    private String studentNo;
    private String grade;
    @TableField("class_name_text")
    private String classNameText;
    @TableField("guardian_name")
    private String guardianName;
    @TableField("guardian_phone")
    private String guardianPhone;
    private String address;
    private String intro;
    @TableField("entry_date")
    private LocalDate entryDate;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getClassNameText() { return classNameText; }
    public void setClassNameText(String classNameText) { this.classNameText = classNameText; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
}
