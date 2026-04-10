package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentCoursePageQueryDTO;
import com.edusmart.manager.entity.EduClassEntity;

public interface StudentCourseService {
    PageData<EduClassEntity> pageCourses(StudentCoursePageQueryDTO queryDTO);
    EduClassEntity getClassDetail(Long classId);
}
