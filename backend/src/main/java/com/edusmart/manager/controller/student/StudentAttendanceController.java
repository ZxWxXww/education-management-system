package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAttendancePageQueryDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.service.student.StudentAttendanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/attendance")
@PreAuthorize("hasRole('STUDENT')")
public class StudentAttendanceController {
    private final StudentAttendanceService attendanceService;
    public StudentAttendanceController(StudentAttendanceService attendanceService) { this.attendanceService = attendanceService; }

    @PostMapping("/page")
    public Result<PageData<EduAttendanceRecordEntity>> page(@RequestBody StudentAttendancePageQueryDTO queryDTO) {
        return Result.success(attendanceService.pageAttendance(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<EduAttendanceRecordEntity> detail(@PathVariable Long id) {
        return Result.success(attendanceService.getAttendance(id));
    }

    @PostMapping("/exceptions/page")
    public Result<PageData<EduAttendanceExceptionEntity>> pageExceptions(@RequestBody StudentAttendanceExceptionPageQueryDTO queryDTO) {
        return Result.success(attendanceService.pageExceptions(queryDTO));
    }

    @GetMapping("/exceptions/{id}")
    public Result<EduAttendanceExceptionEntity> exceptionDetail(@PathVariable Long id) {
        return Result.success(attendanceService.getException(id));
    }
}
