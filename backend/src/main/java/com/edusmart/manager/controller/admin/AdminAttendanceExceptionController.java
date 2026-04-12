package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminAttendanceExceptionPageItemDTO;
import com.edusmart.manager.dto.admin.AttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.admin.AttendanceExceptionSaveDTO;
import com.edusmart.manager.service.admin.AdminAttendanceExceptionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/attendance-exceptions")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('attendance:abnormal:view')")
public class AdminAttendanceExceptionController {
    private final AdminAttendanceExceptionService attendanceExceptionService;

    public AdminAttendanceExceptionController(AdminAttendanceExceptionService attendanceExceptionService) {
        this.attendanceExceptionService = attendanceExceptionService;
    }

    @PostMapping("/page")
    public Result<PageData<AdminAttendanceExceptionPageItemDTO>> page(@RequestBody AttendanceExceptionPageQueryDTO queryDTO) {
        return Result.success(attendanceExceptionService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<AdminAttendanceExceptionPageItemDTO> detail(@PathVariable Long id) {
        return Result.success(attendanceExceptionService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody AttendanceExceptionSaveDTO dto) {
        return Result.success(attendanceExceptionService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AttendanceExceptionSaveDTO dto) {
        attendanceExceptionService.update(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        attendanceExceptionService.delete(id);
        return Result.success(null);
    }
}
