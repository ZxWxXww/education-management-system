package com.edusmart.manager.controller.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionSaveDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.service.teacher.TeacherAttendanceExceptionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/attendance-exceptions")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherAttendanceExceptionController {
    private final TeacherAttendanceExceptionService exceptionService;
    public TeacherAttendanceExceptionController(TeacherAttendanceExceptionService exceptionService) { this.exceptionService = exceptionService; }

    @PostMapping("/page")
    public Result<PageData<EduAttendanceExceptionEntity>> page(@RequestBody TeacherAttendanceExceptionPageQueryDTO queryDTO) { return Result.success(exceptionService.page(queryDTO)); }
    @GetMapping("/{id}")
    public Result<EduAttendanceExceptionEntity> detail(@PathVariable Long id) { return Result.success(exceptionService.getById(id)); }
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TeacherAttendanceExceptionSaveDTO dto) { return Result.success(exceptionService.create(dto)); }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherAttendanceExceptionSaveDTO dto) { exceptionService.update(id, dto); return Result.success(null); }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { exceptionService.delete(id); return Result.success(null); }
}
