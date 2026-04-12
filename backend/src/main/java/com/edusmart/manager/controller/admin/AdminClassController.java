package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminClassPageItemDTO;
import com.edusmart.manager.dto.admin.ClassPageQueryDTO;
import com.edusmart.manager.dto.admin.ClassSaveDTO;
import com.edusmart.manager.service.admin.AdminClassService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/classes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminClassController {
    private final AdminClassService adminClassService;

    public AdminClassController(AdminClassService adminClassService) {
        this.adminClassService = adminClassService;
    }

    @PreAuthorize("hasAnyAuthority('class:view', 'class:manage')")
    @PostMapping("/page")
    public Result<PageData<AdminClassPageItemDTO>> page(@RequestBody ClassPageQueryDTO queryDTO) {
        return Result.success(adminClassService.page(queryDTO));
    }

    @PreAuthorize("hasAnyAuthority('class:view', 'class:manage')")
    @GetMapping("/{id}")
    public Result<AdminClassPageItemDTO> detail(@PathVariable Long id) {
        return Result.success(adminClassService.getById(id));
    }

    @PreAuthorize("hasAuthority('class:manage')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ClassSaveDTO dto) {
        return Result.success(adminClassService.create(dto));
    }

    @PreAuthorize("hasAuthority('class:manage')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ClassSaveDTO dto) {
        adminClassService.update(id, dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('class:manage')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminClassService.delete(id);
        return Result.success(null);
    }
}
