package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherClassDetailDTO;
import com.edusmart.manager.dto.teacher.TeacherClassPageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherClassPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherClassSaveDTO;

public interface TeacherClassService {
    PageData<TeacherClassPageItemDTO> page(TeacherClassPageQueryDTO queryDTO);
    TeacherClassDetailDTO getById(Long id);
    Long create(TeacherClassSaveDTO dto);
    void update(Long id, TeacherClassSaveDTO dto);
    void delete(Long id);
}
