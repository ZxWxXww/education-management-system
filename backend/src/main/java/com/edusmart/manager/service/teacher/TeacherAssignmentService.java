package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAssignmentPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAssignmentPageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAssignmentSaveDTO;

public interface TeacherAssignmentService {
    PageData<TeacherAssignmentPageItemDTO> page(TeacherAssignmentPageQueryDTO queryDTO);
    TeacherAssignmentPageItemDTO getById(Long id);
    Long create(TeacherAssignmentSaveDTO dto);
    void update(Long id, TeacherAssignmentSaveDTO dto);
    void delete(Long id);
}
