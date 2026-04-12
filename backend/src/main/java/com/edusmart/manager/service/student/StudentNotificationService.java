package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentNotificationItemDTO;
import com.edusmart.manager.dto.student.StudentNotificationPageQueryDTO;

public interface StudentNotificationService {
    PageData<StudentNotificationItemDTO> pageNotifications(StudentNotificationPageQueryDTO queryDTO);
    StudentNotificationItemDTO getNotification(Long id);
    void markAsRead(Long notificationId);
}
