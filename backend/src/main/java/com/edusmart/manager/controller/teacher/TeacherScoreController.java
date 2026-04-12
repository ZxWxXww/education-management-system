package com.edusmart.manager.controller.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.teacher.TeacherScorePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherScorePageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherScoreSaveDTO;
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

    @PreAuthorize("hasAnyAuthority('score:manage', 'teacher:grade:analysis:view')")
    @PostMapping("/page")
    public Result<PageData<TeacherScorePageItemDTO>> page(@RequestBody TeacherScorePageQueryDTO queryDTO) { return Result.success(scoreService.page(queryDTO)); }
    @PreAuthorize("hasAnyAuthority('score:manage', 'teacher:grade:analysis:view')")
    @GetMapping("/{id}")
    public Result<TeacherScorePageItemDTO> detail(@PathVariable Long id) { return Result.success(scoreService.getById(id)); }
    @PreAuthorize("hasAuthority('score:manage')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TeacherScoreSaveDTO dto) { return Result.success(scoreService.create(dto)); }
    @PreAuthorize("hasAuthority('score:manage')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherScoreSaveDTO dto) { scoreService.update(id, dto); return Result.success(null); }
    @PreAuthorize("hasAuthority('score:manage')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { scoreService.delete(id); return Result.success(null); }
    @PreAuthorize("hasAnyAuthority('score:manage', 'teacher:grade:analysis:view')")
    @GetMapping("/analysis/{examId}")
    public Result<Map<String, Object>> analysis(@PathVariable Long examId) { return Result.success(scoreService.analysis(examId)); }
}
