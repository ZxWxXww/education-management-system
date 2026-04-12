package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminAssignmentPageItemDTO;
import com.edusmart.manager.dto.admin.AdminAssignmentPageQueryDTO;
import com.edusmart.manager.service.admin.AdminAssignmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/assignments")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('assignment:manage')")
public class AdminAssignmentController {
    private final AdminAssignmentService adminAssignmentService;

    public AdminAssignmentController(AdminAssignmentService adminAssignmentService) {
        this.adminAssignmentService = adminAssignmentService;
    }

    @PostMapping("/page")
    public Result<PageData<AdminAssignmentPageItemDTO>> page(@RequestBody AdminAssignmentPageQueryDTO queryDTO) {
        return Result.success(adminAssignmentService.page(queryDTO));
    }
}
