package com.edusmart.manager.service.student;

import com.edusmart.manager.dto.student.StudentPasswordUpdateDTO;
import com.edusmart.manager.dto.student.StudentProfileUpdateDTO;
import com.edusmart.manager.entity.EduStudentProfileEntity;

public interface StudentProfileService {
    EduStudentProfileEntity getProfileByUserId(Long userId);
    void updateProfile(StudentProfileUpdateDTO dto);
    void updatePassword(StudentPasswordUpdateDTO dto);
}
