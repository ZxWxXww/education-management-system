package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentPasswordUpdateDTO;
import com.edusmart.manager.dto.student.StudentProfileDetailDTO;
import com.edusmart.manager.dto.student.StudentProfileUpdateDTO;
import com.edusmart.manager.service.student.StudentProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileController {
    private final StudentProfileService profileService;

    public StudentProfileController(StudentProfileService profileService) {
        this.profileService = profileService;
    }

    @PreAuthorize("hasAuthority('student:profile:password:update')")
    @GetMapping("/me")
    public Result<StudentProfileDetailDTO> me() {
        return Result.success(profileService.getCurrentProfile());
    }

    @PreAuthorize("hasAuthority('student:profile:password:update')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody StudentProfileUpdateDTO dto) {
        profileService.updateProfile(dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('student:profile:password:update')")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody StudentPasswordUpdateDTO dto) {
        profileService.updatePassword(dto);
        return Result.success(null);
    }
}
