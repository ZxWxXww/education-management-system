package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.CoursePageQueryDTO;
import com.edusmart.manager.dto.admin.CourseSaveDTO;
import com.edusmart.manager.entity.EduCourseEntity;

public interface AdminCourseService {
    PageData<EduCourseEntity> page(CoursePageQueryDTO queryDTO);

    EduCourseEntity getById(Long id);

    Long create(CourseSaveDTO dto);

    void update(Long id, CourseSaveDTO dto);

    void delete(Long id);
}
