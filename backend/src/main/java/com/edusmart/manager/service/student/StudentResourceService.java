package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentResourcePageQueryDTO;
import com.edusmart.manager.entity.EduTeachingResourceEntity;

public interface StudentResourceService {
    PageData<EduTeachingResourceEntity> pageResources(StudentResourcePageQueryDTO queryDTO);
    EduTeachingResourceEntity getResource(Long id);
}
