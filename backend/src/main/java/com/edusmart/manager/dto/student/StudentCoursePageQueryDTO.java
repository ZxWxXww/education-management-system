package com.edusmart.manager.dto.student;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class StudentCoursePageQueryDTO extends BasePageQueryDTO {
    private Long studentId;
    private String keyword;
    private Long courseId;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
}
