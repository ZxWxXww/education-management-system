package com.edusmart.manager.service.teacher;

import com.edusmart.manager.dto.teacher.TeacherPasswordUpdateDTO;
import com.edusmart.manager.entity.EduTeacherProfileEntity;

public interface TeacherProfileService {
    EduTeacherProfileEntity getByUserId(Long userId);
    void updatePassword(TeacherPasswordUpdateDTO dto);
}
