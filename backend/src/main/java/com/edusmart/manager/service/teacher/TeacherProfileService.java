package com.edusmart.manager.service.teacher;

import com.edusmart.manager.dto.teacher.TeacherPasswordUpdateDTO;
import com.edusmart.manager.dto.teacher.TeacherProfileDetailDTO;
import com.edusmart.manager.dto.teacher.TeacherProfileUpdateDTO;

public interface TeacherProfileService {
    TeacherProfileDetailDTO getCurrentProfileDetail();
    TeacherProfileDetailDTO getProfileDetailByUserId(Long userId);
    void updateProfile(TeacherProfileUpdateDTO dto);
    void updatePassword(TeacherPasswordUpdateDTO dto);
}
