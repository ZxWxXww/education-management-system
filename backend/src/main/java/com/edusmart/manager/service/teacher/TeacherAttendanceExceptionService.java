package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionSaveDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;

public interface TeacherAttendanceExceptionService {
    PageData<EduAttendanceExceptionEntity> page(TeacherAttendanceExceptionPageQueryDTO queryDTO);
    EduAttendanceExceptionEntity getById(Long id);
    Long create(TeacherAttendanceExceptionSaveDTO dto);
    void update(Long id, TeacherAttendanceExceptionSaveDTO dto);
    void delete(Long id);
}
