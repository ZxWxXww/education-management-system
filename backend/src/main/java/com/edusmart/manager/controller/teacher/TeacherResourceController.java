package com.edusmart.manager.controller.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.teacher.TeacherResourcePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherResourceSaveDTO;
import com.edusmart.manager.entity.EduTeachingResourceEntity;
import com.edusmart.manager.service.teacher.TeacherResourceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/resources")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherResourceController {
    private final TeacherResourceService resourceService;
    public TeacherResourceController(TeacherResourceService resourceService) { this.resourceService = resourceService; }

    @PostMapping("/page")
    public Result<PageData<EduTeachingResourceEntity>> page(@RequestBody TeacherResourcePageQueryDTO queryDTO) { return Result.success(resourceService.page(queryDTO)); }
    @GetMapping("/{id}")
    public Result<EduTeachingResourceEntity> detail(@PathVariable Long id) { return Result.success(resourceService.getById(id)); }
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TeacherResourceSaveDTO dto) { return Result.success(resourceService.create(dto)); }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherResourceSaveDTO dto) { resourceService.update(id, dto); return Result.success(null); }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { resourceService.delete(id); return Result.success(null); }
}
