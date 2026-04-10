package com.edusmart.manager.dto.teacher;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class TeacherAssignmentPageQueryDTO extends BasePageQueryDTO {
    private String keyword;
    private Long classId;
    private Long courseId;
    private String status;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
