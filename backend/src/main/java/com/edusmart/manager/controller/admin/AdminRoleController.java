package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.RolePermissionAssignDTO;
import com.edusmart.manager.dto.admin.RoleSaveDTO;
import com.edusmart.manager.entity.EduRoleEntity;
import com.edusmart.manager.service.admin.AdminRoleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {
    private final AdminRoleService adminRoleService;

    public AdminRoleController(AdminRoleService adminRoleService) {
        this.adminRoleService = adminRoleService;
    }

    @GetMapping("/page")
    public Result<PageData<EduRoleEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(adminRoleService.page(current, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<EduRoleEntity> detail(@PathVariable Long id) {
        return Result.success(adminRoleService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody RoleSaveDTO dto) {
        return Result.success(adminRoleService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveDTO dto) {
        adminRoleService.update(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminRoleService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionAssignDTO dto) {
        adminRoleService.assignPermissions(id, dto.getPermissionIds());
        return Result.success(null);
    }
}
