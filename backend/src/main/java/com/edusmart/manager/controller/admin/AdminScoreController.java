package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminScorePageItemDTO;
import com.edusmart.manager.dto.admin.AdminScorePageQueryDTO;
import com.edusmart.manager.service.admin.AdminScoreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/scores")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('score:manage')")
public class AdminScoreController {
    private final AdminScoreService adminScoreService;

    public AdminScoreController(AdminScoreService adminScoreService) {
        this.adminScoreService = adminScoreService;
    }

    @PostMapping("/page")
    public Result<PageData<AdminScorePageItemDTO>> page(@RequestBody AdminScorePageQueryDTO queryDTO) {
        return Result.success(adminScoreService.page(queryDTO));
    }
}
