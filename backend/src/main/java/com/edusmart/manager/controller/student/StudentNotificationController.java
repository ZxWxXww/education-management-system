package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentNotificationItemDTO;
import com.edusmart.manager.dto.student.StudentNotificationPageQueryDTO;
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
    public Result<PageData<StudentNotificationItemDTO>> page(@RequestBody StudentNotificationPageQueryDTO queryDTO) {
        return Result.success(notificationService.pageNotifications(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<StudentNotificationItemDTO> detail(@PathVariable Long id) {
        return Result.success(notificationService.getNotification(id));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success(null);
    }
}
