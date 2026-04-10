package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentNotificationPageQueryDTO;
import com.edusmart.manager.entity.EduNotificationEntity;

public interface StudentNotificationService {
    PageData<EduNotificationEntity> pageNotifications(StudentNotificationPageQueryDTO queryDTO);
    EduNotificationEntity getNotification(Long id);
    void markAsRead(Long notificationId, Long userId);
}
