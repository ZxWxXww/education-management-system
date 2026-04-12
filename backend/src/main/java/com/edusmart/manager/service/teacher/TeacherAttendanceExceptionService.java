package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionPageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionSaveDTO;

public interface TeacherAttendanceExceptionService {
    PageData<TeacherAttendanceExceptionPageItemDTO> page(TeacherAttendanceExceptionPageQueryDTO queryDTO);
    TeacherAttendanceExceptionPageItemDTO getById(Long id);
    Long create(TeacherAttendanceExceptionSaveDTO dto);
    void update(Long id, TeacherAttendanceExceptionSaveDTO dto);
    void delete(Long id);
}
