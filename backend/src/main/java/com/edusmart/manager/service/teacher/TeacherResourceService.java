package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherResourcePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherResourcePageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherResourceSaveDTO;

public interface TeacherResourceService {
    PageData<TeacherResourcePageItemDTO> page(TeacherResourcePageQueryDTO queryDTO);
    TeacherResourcePageItemDTO getById(Long id);
    Long create(TeacherResourceSaveDTO dto);
    void update(Long id, TeacherResourceSaveDTO dto);
    void delete(Long id);
}
