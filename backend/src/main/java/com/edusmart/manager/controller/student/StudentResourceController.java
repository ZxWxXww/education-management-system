package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentResourcePageQueryDTO;
import com.edusmart.manager.dto.student.StudentResourcePageItemDTO;
import com.edusmart.manager.service.student.StudentResourceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/resources")
@PreAuthorize("hasRole('STUDENT') and hasAuthority('student:resource:view')")
public class StudentResourceController {
    private final StudentResourceService resourceService;
    public StudentResourceController(StudentResourceService resourceService) { this.resourceService = resourceService; }

    @PostMapping("/page")
    public Result<PageData<StudentResourcePageItemDTO>> page(@RequestBody StudentResourcePageQueryDTO queryDTO) {
        return Result.success(resourceService.pageResources(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<StudentResourcePageItemDTO> detail(@PathVariable Long id) {
        return Result.success(resourceService.getResource(id));
    }
}
