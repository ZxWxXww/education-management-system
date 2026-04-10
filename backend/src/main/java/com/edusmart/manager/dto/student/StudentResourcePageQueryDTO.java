package com.edusmart.manager.dto.student;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class StudentResourcePageQueryDTO extends BasePageQueryDTO {
    private Long classId;
    private Long courseId;
    private String keyword;
    private Integer status;

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
