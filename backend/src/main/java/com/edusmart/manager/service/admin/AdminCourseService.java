package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminCoursePageItemDTO;
import com.edusmart.manager.dto.admin.CoursePageQueryDTO;
import com.edusmart.manager.dto.admin.CourseSaveDTO;

public interface AdminCourseService {
    PageData<AdminCoursePageItemDTO> page(CoursePageQueryDTO queryDTO);

    AdminCoursePageItemDTO getById(Long id);

    Long create(CourseSaveDTO dto);

    void update(Long id, CourseSaveDTO dto);

    void delete(Long id);
}
