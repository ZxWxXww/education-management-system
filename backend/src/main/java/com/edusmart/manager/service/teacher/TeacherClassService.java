package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherClassPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherClassSaveDTO;
import com.edusmart.manager.entity.EduClassEntity;

public interface TeacherClassService {
    PageData<EduClassEntity> page(TeacherClassPageQueryDTO queryDTO);
    EduClassEntity getById(Long id);
    Long create(TeacherClassSaveDTO dto);
    void update(Long id, TeacherClassSaveDTO dto);
    void delete(Long id);
}
