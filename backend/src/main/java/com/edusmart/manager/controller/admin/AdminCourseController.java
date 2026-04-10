package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.CoursePageQueryDTO;
import com.edusmart.manager.dto.admin.CourseSaveDTO;
import com.edusmart.manager.entity.EduCourseEntity;
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

    @PostMapping("/page")
    public Result<PageData<EduCourseEntity>> page(@RequestBody CoursePageQueryDTO queryDTO) {
        return Result.success(adminCourseService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<EduCourseEntity> detail(@PathVariable Long id) {
        return Result.success(adminCourseService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody CourseSaveDTO dto) {
        return Result.success(adminCourseService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CourseSaveDTO dto) {
        adminCourseService.update(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCourseService.delete(id);
        return Result.success(null);
    }
}
