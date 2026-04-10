package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentNotificationPageQueryDTO;
import com.edusmart.manager.entity.EduNotificationEntity;
import com.edusmart.manager.service.student.StudentNotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/notifications")
@PreAuthorize("hasRole('STUDENT')")
public class StudentNotificationController {
    private final StudentNotificationService notificationService;
    public StudentNotificationController(StudentNotificationService notificationService) { this.notificationService = notificationService; }

    @PostMapping("/page")
    public Result<PageData<EduNotificationEntity>> page(@RequestBody StudentNotificationPageQueryDTO queryDTO) {
        return Result.success(notificationService.pageNotifications(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<EduNotificationEntity> detail(@PathVariable Long id) {
        return Result.success(notificationService.getNotification(id));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        return Result.success(null);
    }
}
