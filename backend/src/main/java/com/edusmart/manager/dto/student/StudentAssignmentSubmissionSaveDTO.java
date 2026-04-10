package com.edusmart.manager.dto.student;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class StudentAssignmentSubmissionSaveDTO {
    @NotNull(message = "作业ID不能为空")
    private Long assignmentId;
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    private LocalDateTime submitTime;
    private String submitContent;
    private String attachmentType;
    private String attachmentUrl;
    private String status = "SUBMITTED";

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
    public String getSubmitContent() { return submitContent; }
    public void setSubmitContent(String submitContent) { this.submitContent = submitContent; }
    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
