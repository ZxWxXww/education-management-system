package com.edusmart.manager.dto.admin;

import java.time.LocalDate;

public class AdminAttendanceTrendPointDTO {
    private String label;
    private LocalDate attendanceDate;
    private Double punctualRate;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public Double getPunctualRate() {
        return punctualRate;
    }

    public void setPunctualRate(Double punctualRate) {
        this.punctualRate = punctualRate;
    }
}
