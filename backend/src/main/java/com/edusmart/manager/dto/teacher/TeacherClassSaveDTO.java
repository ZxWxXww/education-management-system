package com.edusmart.manager.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TeacherClassSaveDTO {
    @NotBlank(message = "班级编码不能为空")
    private String classCode;
    @NotBlank(message = "班级名称不能为空")
    private String className;
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status = 1;

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
