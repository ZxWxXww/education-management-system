package com.edusmart.manager.controller.teacher;

import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.teacher.TeacherPasswordUpdateDTO;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.service.teacher.TeacherProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/profile")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherProfileController {
    private final TeacherProfileService profileService;
    public TeacherProfileController(TeacherProfileService profileService) { this.profileService = profileService; }

    @GetMapping("/{userId}")
    public Result<EduTeacherProfileEntity> detail(@PathVariable Long userId) { return Result.success(profileService.getByUserId(userId)); }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody TeacherPasswordUpdateDTO dto) {
        profileService.updatePassword(dto);
        return Result.success(null);
    }
}
