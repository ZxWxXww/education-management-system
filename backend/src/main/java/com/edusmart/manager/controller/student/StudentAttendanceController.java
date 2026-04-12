package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentAttendanceExceptionPageItemDTO;
import com.edusmart.manager.dto.student.StudentAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAttendancePageItemDTO;
import com.edusmart.manager.dto.student.StudentAttendancePageQueryDTO;
import com.edusmart.manager.service.student.StudentAttendanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/attendance")
@PreAuthorize("hasRole('STUDENT') and hasAuthority('student:attendance:abnormal:view')")
public class StudentAttendanceController {
    private final StudentAttendanceService attendanceService;

    public StudentAttendanceController(StudentAttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/page")
    public Result<PageData<StudentAttendancePageItemDTO>> page(@RequestBody StudentAttendancePageQueryDTO queryDTO) {
        return Result.success(attendanceService.pageAttendance(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<StudentAttendancePageItemDTO> detail(@PathVariable Long id) {
        return Result.success(attendanceService.getAttendance(id));
    }

    @PostMapping("/exceptions/page")
    public Result<PageData<StudentAttendanceExceptionPageItemDTO>> pageExceptions(@RequestBody StudentAttendanceExceptionPageQueryDTO queryDTO) {
        return Result.success(attendanceService.pageExceptions(queryDTO));
    }

    @GetMapping("/exceptions/{id}")
    public Result<StudentAttendanceExceptionPageItemDTO> exceptionDetail(@PathVariable Long id) {
        return Result.success(attendanceService.getException(id));
    }
}
