package com.edusmart.manager.controller.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.teacher.TeacherAttendanceBatchTaskPageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceBatchTaskPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceBatchUpdateDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendancePageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendancePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceSaveDTO;
import com.edusmart.manager.dto.teacher.TeacherBatchAttendanceSaveDTO;
import com.edusmart.manager.service.teacher.TeacherAttendanceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/attendance")
@PreAuthorize("hasRole('TEACHER') and hasAuthority('teacher:attendance:batch:manage')")
public class TeacherAttendanceController {
    private final TeacherAttendanceService attendanceService;
    public TeacherAttendanceController(TeacherAttendanceService attendanceService) { this.attendanceService = attendanceService; }

    @PostMapping("/page")
    public Result<PageData<TeacherAttendancePageItemDTO>> page(@RequestBody TeacherAttendancePageQueryDTO queryDTO) { return Result.success(attendanceService.page(queryDTO)); }
    @GetMapping("/{id}")
    public Result<TeacherAttendancePageItemDTO> detail(@PathVariable Long id) { return Result.success(attendanceService.getById(id)); }
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TeacherAttendanceSaveDTO dto) { return Result.success(attendanceService.create(dto)); }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherAttendanceSaveDTO dto) { attendanceService.update(id, dto); return Result.success(null); }
    @PutMapping("/batch-update")
    public Result<Void> batchUpdate(@Valid @RequestBody TeacherAttendanceBatchUpdateDTO dto) { attendanceService.batchUpdate(dto); return Result.success(null); }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { attendanceService.delete(id); return Result.success(null); }

    @PostMapping("/batch")
    public Result<Long> createBatch(@Valid @RequestBody TeacherBatchAttendanceSaveDTO dto) { return Result.success(attendanceService.createBatchTask(dto)); }
    @GetMapping("/batch/{id}")
    public Result<TeacherAttendanceBatchTaskPageItemDTO> batchDetail(@PathVariable Long id) { return Result.success(attendanceService.getBatchTask(id)); }
    @PostMapping("/batch/page")
    public Result<PageData<TeacherAttendanceBatchTaskPageItemDTO>> batchPage(@RequestBody TeacherAttendanceBatchTaskPageQueryDTO queryDTO) { return Result.success(attendanceService.pageBatchTasks(queryDTO)); }
}
