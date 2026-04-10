package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminUserPageQueryDTO;
import com.edusmart.manager.dto.admin.AdminUserSaveDTO;
import com.edusmart.manager.dto.admin.UserRoleAssignDTO;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.service.admin.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping("/page")
    public Result<PageData<EduUserEntity>> page(@RequestBody AdminUserPageQueryDTO queryDTO) {
        return Result.success(adminUserService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<EduUserEntity> detail(@PathVariable Long id) {
        return Result.success(adminUserService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody AdminUserSaveDTO dto) {
        return Result.success(adminUserService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AdminUserSaveDTO dto) {
        adminUserService.update(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody UserRoleAssignDTO dto) {
        adminUserService.assignRoles(id, dto.getRoleIds());
        return Result.success(null);
    }
}
