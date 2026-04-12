package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminOrderDetailDTO;
import com.edusmart.manager.dto.admin.AdminOrderPageItemDTO;
import com.edusmart.manager.dto.admin.AdminOrderPageQueryDTO;
import com.edusmart.manager.dto.admin.AdminOrderSaveDTO;
import com.edusmart.manager.service.admin.AdminOrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @PreAuthorize("hasAuthority('finance:order:manage')")
    @PostMapping("/page")
    public Result<PageData<AdminOrderPageItemDTO>> page(@RequestBody AdminOrderPageQueryDTO queryDTO) {
        return Result.success(adminOrderService.page(queryDTO));
    }

    @PreAuthorize("hasAuthority('finance:order:manage')")
    @GetMapping("/{id}")
    public Result<AdminOrderDetailDTO> detail(@PathVariable Long id) {
        return Result.success(adminOrderService.getById(id));
    }

    @PreAuthorize("hasAuthority('finance:order:manage')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody AdminOrderSaveDTO dto) {
        return Result.success(adminOrderService.create(dto));
    }
}
