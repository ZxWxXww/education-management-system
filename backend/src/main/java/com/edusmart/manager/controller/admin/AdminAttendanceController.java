package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminAttendanceOverviewDTO;
import com.edusmart.manager.service.admin.AdminAttendanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/attendance")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('attendance:view')")
public class AdminAttendanceController {
    private final AdminAttendanceService adminAttendanceService;

    public AdminAttendanceController(AdminAttendanceService adminAttendanceService) {
        this.adminAttendanceService = adminAttendanceService;
    }

    @GetMapping("/overview")
    public Result<AdminAttendanceOverviewDTO> overview() {
        return Result.success(adminAttendanceService.getOverview());
    }
}
