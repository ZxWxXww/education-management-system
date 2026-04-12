package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminDisplayConfigDTO;
import com.edusmart.manager.dto.admin.AdminDisplaySaveDTO;
import com.edusmart.manager.service.admin.AdminDisplayService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/display")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('setting:display:manage')")
public class AdminDisplayController {
    private final AdminDisplayService adminDisplayService;

    public AdminDisplayController(AdminDisplayService adminDisplayService) {
        this.adminDisplayService = adminDisplayService;
    }

    @GetMapping("/current")
    public Result<AdminDisplayConfigDTO> current() {
        return Result.success(adminDisplayService.getCurrent());
    }

    @PutMapping("/current")
    public Result<Void> save(@RequestBody AdminDisplaySaveDTO dto) {
        adminDisplayService.save(dto);
        return Result.success(null);
    }

    @PostMapping("/reset")
    public Result<AdminDisplayConfigDTO> reset() {
        return Result.success(adminDisplayService.reset());
    }
}
