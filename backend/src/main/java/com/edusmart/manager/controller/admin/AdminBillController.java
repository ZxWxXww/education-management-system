package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminBillPageItemDTO;
import com.edusmart.manager.dto.admin.AdminBillPaymentItemDTO;
import com.edusmart.manager.dto.admin.AdminBillPaymentSaveDTO;
import com.edusmart.manager.dto.admin.AdminBillRefundSaveDTO;
import com.edusmart.manager.dto.admin.AdminBillStudentOptionDTO;
import com.edusmart.manager.dto.admin.BillPageQueryDTO;
import com.edusmart.manager.dto.admin.BillSaveDTO;
import com.edusmart.manager.service.admin.AdminBillService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/bills")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('finance:bill:manage')")
public class AdminBillController {
    private final AdminBillService adminBillService;

    public AdminBillController(AdminBillService adminBillService) {
        this.adminBillService = adminBillService;
    }

    @PostMapping("/page")
    public Result<PageData<AdminBillPageItemDTO>> page(@RequestBody BillPageQueryDTO queryDTO) {
        return Result.success(adminBillService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<AdminBillPageItemDTO> detail(@PathVariable Long id) {
        return Result.success(adminBillService.getById(id));
    }

    @GetMapping("/student-options")
    public Result<List<AdminBillStudentOptionDTO>> studentOptions() {
        return Result.success(adminBillService.listStudentOptions());
    }

    @GetMapping("/{id}/payments")
    public Result<List<AdminBillPaymentItemDTO>> paymentList(@PathVariable Long id) {
        return Result.success(adminBillService.listPayments(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody BillSaveDTO dto) {
        return Result.success(adminBillService.create(dto));
    }

    @PostMapping("/{id}/payments")
    public Result<Long> registerPayment(@PathVariable Long id, @Valid @RequestBody AdminBillPaymentSaveDTO dto) {
        return Result.success(adminBillService.registerPayment(id, dto));
    }

    @PostMapping("/{id}/refunds")
    public Result<Long> registerRefund(@PathVariable Long id, @Valid @RequestBody AdminBillRefundSaveDTO dto) {
        return Result.success(adminBillService.registerRefund(id, dto));
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
