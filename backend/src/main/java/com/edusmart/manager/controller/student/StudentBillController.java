package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentBillPageItemDTO;
import com.edusmart.manager.dto.student.StudentBillPageQueryDTO;
import com.edusmart.manager.dto.student.StudentHourPackageSummaryDTO;
import com.edusmart.manager.service.student.StudentBillService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/student/bills")
@PreAuthorize("hasRole('STUDENT')")
public class StudentBillController {
    private final StudentBillService studentBillService;

    public StudentBillController(StudentBillService studentBillService) {
        this.studentBillService = studentBillService;
    }

    @PostMapping("/page")
    public Result<PageData<StudentBillPageItemDTO>> page(@RequestBody StudentBillPageQueryDTO queryDTO) {
        return Result.success(studentBillService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<StudentBillPageItemDTO> detail(@PathVariable Long id) {
        return Result.success(studentBillService.getById(id));
    }

    @GetMapping("/hour-packages/summary")
    public Result<StudentHourPackageSummaryDTO> hourPackageSummary() {
        return Result.success(studentBillService.getHourPackageSummary());
    }
}
