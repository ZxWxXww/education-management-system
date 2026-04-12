package com.edusmart.manager.service.student;

import com.edusmart.manager.dto.student.StudentPasswordUpdateDTO;
import com.edusmart.manager.dto.student.StudentProfileDetailDTO;
import com.edusmart.manager.dto.student.StudentProfileUpdateDTO;

public interface StudentProfileService {
    StudentProfileDetailDTO getCurrentProfile();

    void updateProfile(StudentProfileUpdateDTO dto);

    void updatePassword(StudentPasswordUpdateDTO dto);
}
