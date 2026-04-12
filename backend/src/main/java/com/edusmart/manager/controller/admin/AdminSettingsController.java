package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminSettingsOverviewDTO;
import com.edusmart.manager.service.admin.AdminSettingsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('setting:view')")
public class AdminSettingsController {
    private final AdminSettingsService adminSettingsService;

    public AdminSettingsController(AdminSettingsService adminSettingsService) {
        this.adminSettingsService = adminSettingsService;
    }

    @GetMapping("/overview")
    public Result<AdminSettingsOverviewDTO> overview() {
        return Result.success(adminSettingsService.getOverview());
    }
}
