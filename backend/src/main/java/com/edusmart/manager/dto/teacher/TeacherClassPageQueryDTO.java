package com.edusmart.manager.dto.teacher;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class TeacherClassPageQueryDTO extends BasePageQueryDTO {
    private String keyword;
    private Long courseId;
    private Integer status;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
