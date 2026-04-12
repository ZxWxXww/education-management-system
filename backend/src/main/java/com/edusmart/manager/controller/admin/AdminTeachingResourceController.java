package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminTeachingResourceOverviewDTO;
import com.edusmart.manager.service.admin.AdminTeachingResourceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/resources")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('resource:view')")
public class AdminTeachingResourceController {
    private final AdminTeachingResourceService adminTeachingResourceService;

    public AdminTeachingResourceController(AdminTeachingResourceService adminTeachingResourceService) {
        this.adminTeachingResourceService = adminTeachingResourceService;
    }

    @GetMapping("/overview")
    public Result<AdminTeachingResourceOverviewDTO> overview() {
        return Result.success(adminTeachingResourceService.getOverview());
    }
}
