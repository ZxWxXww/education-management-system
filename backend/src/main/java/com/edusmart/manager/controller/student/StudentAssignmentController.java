package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentAssignmentPageItemDTO;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionSaveDTO;
import com.edusmart.manager.service.student.StudentAssignmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/assignments")
@PreAuthorize("hasRole('STUDENT') and hasAuthority('student:assignment:submission:view')")
public class StudentAssignmentController {
    private final StudentAssignmentService assignmentService;
    public StudentAssignmentController(StudentAssignmentService assignmentService) { this.assignmentService = assignmentService; }

    @PostMapping("/submissions/page")
    public Result<PageData<StudentAssignmentPageItemDTO>> page(@RequestBody StudentAssignmentSubmissionPageQueryDTO queryDTO) {
        return Result.success(assignmentService.pageSubmissions(queryDTO));
    }

    @GetMapping("/submissions/{id}")
    public Result<StudentAssignmentPageItemDTO> detail(@PathVariable Long id) {
        return Result.success(assignmentService.getSubmission(id));
    }

    @PostMapping("/submissions")
    public Result<Long> create(@Valid @RequestBody StudentAssignmentSubmissionSaveDTO dto) {
        return Result.success(assignmentService.createSubmission(dto));
    }

    @PutMapping("/submissions/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StudentAssignmentSubmissionSaveDTO dto) {
        assignmentService.updateSubmission(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/submissions/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assignmentService.deleteSubmission(id);
        return Result.success(null);
    }
}
