package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentExamScorePageQueryDTO;
import com.edusmart.manager.dto.student.StudentScorePageItemDTO;
import com.edusmart.manager.service.student.StudentScoreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/scores")
@PreAuthorize("hasRole('STUDENT')")
public class StudentScoreController {
    private final StudentScoreService scoreService;
    public StudentScoreController(StudentScoreService scoreService) { this.scoreService = scoreService; }

    @PreAuthorize("hasAnyAuthority('student:exam:score:view', 'student:grade:analysis:view')")
    @PostMapping("/page")
    public Result<PageData<StudentScorePageItemDTO>> page(@RequestBody StudentExamScorePageQueryDTO queryDTO) {
        return Result.success(scoreService.pageScores(queryDTO));
    }

    @PreAuthorize("hasAnyAuthority('student:exam:score:view', 'student:grade:analysis:view')")
    @GetMapping("/{id}")
    public Result<StudentScorePageItemDTO> detail(@PathVariable Long id) {
        return Result.success(scoreService.getScore(id));
    }
}
