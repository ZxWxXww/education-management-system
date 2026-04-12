package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminCoursePageItemDTO;
import com.edusmart.manager.dto.admin.CoursePageQueryDTO;
import com.edusmart.manager.dto.admin.CourseSaveDTO;
import com.edusmart.manager.service.admin.AdminCourseService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {
    private final AdminCourseService adminCourseService;

    public AdminCourseController(AdminCourseService adminCourseService) {
        this.adminCourseService = adminCourseService;
    }

    @PreAuthorize("hasAnyAuthority('course:view', 'course:manage')")
    @PostMapping("/page")
    public Result<PageData<AdminCoursePageItemDTO>> page(@RequestBody CoursePageQueryDTO queryDTO) {
        return Result.success(adminCourseService.page(queryDTO));
    }

    @PreAuthorize("hasAnyAuthority('course:view', 'course:manage')")
    @GetMapping("/{id}")
    public Result<AdminCoursePageItemDTO> detail(@PathVariable Long id) {
        return Result.success(adminCourseService.getById(id));
    }

    @PreAuthorize("hasAuthority('course:manage')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CourseSaveDTO dto) {
        return Result.success(adminCourseService.create(dto));
    }

    @PreAuthorize("hasAuthority('course:manage')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CourseSaveDTO dto) {
        adminCourseService.update(id, dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('course:manage')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCourseService.delete(id);
        return Result.success(null);
    }
}
