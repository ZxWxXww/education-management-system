package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminRolePageItemDTO;
import com.edusmart.manager.dto.admin.RolePermissionAssignDTO;
import com.edusmart.manager.dto.admin.RoleSaveDTO;
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

    @PreAuthorize("hasAnyAuthority('user:role:manage', 'user:authorization:manage')")
    @GetMapping("/page")
    public Result<PageData<AdminRolePageItemDTO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(adminRoleService.page(current, size, keyword));
    }

    @PreAuthorize("hasAnyAuthority('user:role:manage', 'user:authorization:manage')")
    @GetMapping("/{id}")
    public Result<AdminRolePageItemDTO> detail(@PathVariable Long id) {
        return Result.success(adminRoleService.getById(id));
    }

    @PreAuthorize("hasAuthority('user:role:manage')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody RoleSaveDTO dto) {
        return Result.success(adminRoleService.create(dto));
    }

    @PreAuthorize("hasAuthority('user:role:manage')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveDTO dto) {
        adminRoleService.update(id, dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('user:role:manage')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminRoleService.delete(id);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('user:role:manage')")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionAssignDTO dto) {
        adminRoleService.assignPermissions(id, dto.getPermissionIds());
        return Result.success(null);
    }
}
