package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherResourcePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherResourceSaveDTO;
import com.edusmart.manager.entity.EduTeachingResourceEntity;

public interface TeacherResourceService {
    PageData<EduTeachingResourceEntity> page(TeacherResourcePageQueryDTO queryDTO);
    EduTeachingResourceEntity getById(Long id);
    Long create(TeacherResourceSaveDTO dto);
    void update(Long id, TeacherResourceSaveDTO dto);
    void delete(Long id);
}
