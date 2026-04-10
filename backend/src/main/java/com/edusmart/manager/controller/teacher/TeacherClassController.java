package com.edusmart.manager.controller.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.teacher.TeacherClassPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherClassSaveDTO;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.service.teacher.TeacherClassService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/classes")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherClassController {
    private final TeacherClassService classService;
    public TeacherClassController(TeacherClassService classService) { this.classService = classService; }

    @PostMapping("/page")
    public Result<PageData<EduClassEntity>> page(@RequestBody TeacherClassPageQueryDTO queryDTO) { return Result.success(classService.page(queryDTO)); }
    @GetMapping("/{id}")
    public Result<EduClassEntity> detail(@PathVariable Long id) { return Result.success(classService.getById(id)); }
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TeacherClassSaveDTO dto) { return Result.success(classService.create(dto)); }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherClassSaveDTO dto) { classService.update(id, dto); return Result.success(null); }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { classService.delete(id); return Result.success(null); }
}
