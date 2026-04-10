package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.BillPageQueryDTO;
import com.edusmart.manager.dto.admin.BillSaveDTO;
import com.edusmart.manager.entity.EduBillEntity;
import com.edusmart.manager.service.admin.AdminBillService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/bills")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBillController {
    private final AdminBillService adminBillService;

    public AdminBillController(AdminBillService adminBillService) {
        this.adminBillService = adminBillService;
    }

    @PostMapping("/page")
    public Result<PageData<EduBillEntity>> page(@RequestBody BillPageQueryDTO queryDTO) {
        return Result.success(adminBillService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<EduBillEntity> detail(@PathVariable Long id) {
        return Result.success(adminBillService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody BillSaveDTO dto) {
        return Result.success(adminBillService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BillSaveDTO dto) {
        adminBillService.update(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminBillService.delete(id);
        return Result.success(null);
    }
}
