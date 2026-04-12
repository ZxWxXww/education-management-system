package com.edusmart.manager.dto.admin;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class BillPageQueryDTO extends BasePageQueryDTO {
    private String keyword;
    private Long studentId;
    private String billType;
    private String status;
    private String month;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
