package com.edusmart.manager.dto.student;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class StudentNotificationPageQueryDTO extends BasePageQueryDTO {
    private Long userId;
    private String noticeType;
    private Integer isRead;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNoticeType() { return noticeType; }
    public void setNoticeType(String noticeType) { this.noticeType = noticeType; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
}
