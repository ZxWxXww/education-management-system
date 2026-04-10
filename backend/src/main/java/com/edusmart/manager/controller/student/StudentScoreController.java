package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentExamScorePageQueryDTO;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.service.student.StudentScoreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/scores")
@PreAuthorize("hasRole('STUDENT')")
public class StudentScoreController {
    private final StudentScoreService scoreService;
    public StudentScoreController(StudentScoreService scoreService) { this.scoreService = scoreService; }

    @PostMapping("/page")
    public Result<PageData<EduExamScoreEntity>> page(@RequestBody StudentExamScorePageQueryDTO queryDTO) {
        return Result.success(scoreService.pageScores(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<EduExamScoreEntity> detail(@PathVariable Long id) {
        return Result.success(scoreService.getScore(id));
    }
}
