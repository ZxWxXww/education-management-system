package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("edu_notification")
public class EduNotificationEntity extends BaseEntity {
    @TableField("notice_type")
    private String noticeType;
    private String title;
    private String content;
    @TableField("target_role")
    private String targetRole;
    @TableField("target_user_id")
    private Long targetUserId;
    @TableField("biz_ref_type")
    private String bizRefType;
    @TableField("biz_ref_id")
    private Long bizRefId;
    private String status;
    @TableField("publish_time")
    private LocalDateTime publishTime;

    public String getNoticeType() { return noticeType; }
    public void setNoticeType(String noticeType) { this.noticeType = noticeType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getBizRefType() { return bizRefType; }
    public void setBizRefType(String bizRefType) { this.bizRefType = bizRefType; }
    public Long getBizRefId() { return bizRefId; }
    public void setBizRefId(Long bizRefId) { this.bizRefId = bizRefId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
}
