package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("edu_assignment_submission")
public class EduAssignmentSubmissionEntity extends BaseEntity {
    @TableField("assignment_id")
    private Long assignmentId;
    @TableField("student_id")
    private Long studentId;
    @TableField("submit_time")
    private LocalDateTime submitTime;
    @TableField("submit_content")
    private String submitContent;
    @TableField("attachment_type")
    private String attachmentType;
    @TableField("attachment_url")
    private String attachmentUrl;
    private BigDecimal score;
    private String feedback;
    private String status;

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
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
