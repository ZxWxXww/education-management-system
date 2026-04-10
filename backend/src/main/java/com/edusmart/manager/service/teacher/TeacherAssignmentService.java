package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAssignmentPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAssignmentSaveDTO;
import com.edusmart.manager.entity.EduAssignmentEntity;

public interface TeacherAssignmentService {
    PageData<EduAssignmentEntity> page(TeacherAssignmentPageQueryDTO queryDTO);
    EduAssignmentEntity getById(Long id);
    Long create(TeacherAssignmentSaveDTO dto);
    void update(Long id, TeacherAssignmentSaveDTO dto);
    void delete(Long id);
}
