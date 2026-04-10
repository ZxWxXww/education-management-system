package com.edusmart.manager.dto.student;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class StudentAssignmentSubmissionPageQueryDTO extends BasePageQueryDTO {
    private Long studentId;
    private Long assignmentId;
    private String status;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
