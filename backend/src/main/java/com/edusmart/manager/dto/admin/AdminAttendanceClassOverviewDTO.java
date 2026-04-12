package com.edusmart.manager.dto.admin;

public class AdminAttendanceClassOverviewDTO {
    private Long classId;
    private String className;
    private String teacherName;
    private Long shouldAttend;
    private Long actualAttend;
    private Double normalRate;
    private Long abnormalCount;

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Long getShouldAttend() {
        return shouldAttend;
    }

    public void setShouldAttend(Long shouldAttend) {
        this.shouldAttend = shouldAttend;
    }

    public Long getActualAttend() {
        return actualAttend;
    }

    public void setActualAttend(Long actualAttend) {
        this.actualAttend = actualAttend;
    }

    public Double getNormalRate() {
        return normalRate;
    }

    public void setNormalRate(Double normalRate) {
        this.normalRate = normalRate;
    }

    public Long getAbnormalCount() {
        return abnormalCount;
    }

    public void setAbnormalCount(Long abnormalCount) {
        this.abnormalCount = abnormalCount;
    }
}
