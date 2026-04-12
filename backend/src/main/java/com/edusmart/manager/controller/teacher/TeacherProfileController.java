package com.edusmart.manager.controller.teacher;

import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.teacher.TeacherPasswordUpdateDTO;
import com.edusmart.manager.dto.teacher.TeacherProfileDetailDTO;
import com.edusmart.manager.dto.teacher.TeacherProfileUpdateDTO;
import com.edusmart.manager.service.teacher.TeacherProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/profile")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherProfileController {
    private final TeacherProfileService profileService;

    public TeacherProfileController(TeacherProfileService profileService) {
        this.profileService = profileService;
    }

    @PreAuthorize("hasAuthority('teacher:profile:password:update')")
    @GetMapping("/me")
    public Result<TeacherProfileDetailDTO> me() {
        return Result.success(profileService.getCurrentProfileDetail());
    }

    @PreAuthorize("hasAuthority('teacher:profile:password:update')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody TeacherProfileUpdateDTO dto) {
        profileService.updateProfile(dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('teacher:profile:password:update')")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody TeacherPasswordUpdateDTO dto) {
        profileService.updatePassword(dto);
        return Result.success(null);
    }
}
