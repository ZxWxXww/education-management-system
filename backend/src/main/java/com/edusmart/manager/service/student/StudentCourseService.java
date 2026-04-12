package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentCourseDetailDTO;
import com.edusmart.manager.dto.student.StudentCoursePageItemDTO;
import com.edusmart.manager.dto.student.StudentCoursePageQueryDTO;

public interface StudentCourseService {
    PageData<StudentCoursePageItemDTO> pageCourses(StudentCoursePageQueryDTO queryDTO);
    StudentCourseDetailDTO getClassDetail(Long classId);
}
