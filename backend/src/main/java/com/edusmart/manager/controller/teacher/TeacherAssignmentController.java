package com.edusmart.manager.controller.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.teacher.TeacherAssignmentPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAssignmentSaveDTO;
import com.edusmart.manager.entity.EduAssignmentEntity;
import com.edusmart.manager.service.teacher.TeacherAssignmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/assignments")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherAssignmentController {
    private final TeacherAssignmentService assignmentService;
    public TeacherAssignmentController(TeacherAssignmentService assignmentService) { this.assignmentService = assignmentService; }

    @PostMapping("/page")
    public Result<PageData<EduAssignmentEntity>> page(@RequestBody TeacherAssignmentPageQueryDTO queryDTO) { return Result.success(assignmentService.page(queryDTO)); }
    @GetMapping("/{id}")
    public Result<EduAssignmentEntity> detail(@PathVariable Long id) { return Result.success(assignmentService.getById(id)); }
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TeacherAssignmentSaveDTO dto) { return Result.success(assignmentService.create(dto)); }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherAssignmentSaveDTO dto) { assignmentService.update(id, dto); return Result.success(null); }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { assignmentService.delete(id); return Result.success(null); }
}
