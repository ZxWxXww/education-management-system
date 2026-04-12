package com.edusmart.manager.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class StudentProfileUpdateDTO {
    @NotBlank(message = "姓名不能为空")
    private String realName;
    private String grade;
    private String className;
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
    private String email;
    private String guardianName;
    private String guardianPhone;
    private String address;
    private String intro;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
}
