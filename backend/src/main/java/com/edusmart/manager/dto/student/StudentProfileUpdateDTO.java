package com.edusmart.manager.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class StudentProfileUpdateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "姓名不能为空")
    private String realName;
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
    private String email;
    private String address;
    private String intro;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
}
