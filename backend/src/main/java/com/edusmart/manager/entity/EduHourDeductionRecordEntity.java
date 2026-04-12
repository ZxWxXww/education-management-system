package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("edu_hour_deduction_record")
public class EduHourDeductionRecordEntity extends BaseEntity {
    @TableField("hour_package_id")
    private Long hourPackageId;

    @TableField("student_id")
    private Long studentId;

    @TableField("class_id")
    private Long classId;

    @TableField("session_id")
    private Long sessionId;

    @TableField("deduct_hours")
    private BigDecimal deductHours;

    @TableField("deduct_type")
    private String deductType;

    @TableField("biz_date")
    private LocalDate bizDate;

    private String remark;

    public Long getHourPackageId() {
        return hourPackageId;
    }

    public void setHourPackageId(Long hourPackageId) {
        this.hourPackageId = hourPackageId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public BigDecimal getDeductHours() {
        return deductHours;
    }

    public void setDeductHours(BigDecimal deductHours) {
        this.deductHours = deductHours;
    }

    public String getDeductType() {
        return deductType;
    }

    public void setDeductType(String deductType) {
        this.deductType = deductType;
    }

    public LocalDate getBizDate() {
        return bizDate;
    }

    public void setBizDate(LocalDate bizDate) {
        this.bizDate = bizDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
