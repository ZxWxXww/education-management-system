package com.edusmart.manager.controller.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.teacher.TeacherScorePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherScoreSaveDTO;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.service.teacher.TeacherScoreService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher/scores")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherScoreController {
    private final TeacherScoreService scoreService;
    public TeacherScoreController(TeacherScoreService scoreService) { this.scoreService = scoreService; }

    @PostMapping("/page")
    public Result<PageData<EduExamScoreEntity>> page(@RequestBody TeacherScorePageQueryDTO queryDTO) { return Result.success(scoreService.page(queryDTO)); }
    @GetMapping("/{id}")
    public Result<EduExamScoreEntity> detail(@PathVariable Long id) { return Result.success(scoreService.getById(id)); }
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TeacherScoreSaveDTO dto) { return Result.success(scoreService.create(dto)); }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherScoreSaveDTO dto) { scoreService.update(id, dto); return Result.success(null); }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { scoreService.delete(id); return Result.success(null); }
    @GetMapping("/analysis/{examId}")
    public Result<Map<String, Object>> analysis(@PathVariable Long examId) { return Result.success(scoreService.analysis(examId)); }
}
