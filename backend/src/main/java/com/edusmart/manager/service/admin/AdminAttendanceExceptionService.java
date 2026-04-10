package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.admin.AttendanceExceptionSaveDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;

public interface AdminAttendanceExceptionService {
    PageData<EduAttendanceExceptionEntity> page(AttendanceExceptionPageQueryDTO queryDTO);

    EduAttendanceExceptionEntity getById(Long id);

    Long create(AttendanceExceptionSaveDTO dto);

    void update(Long id, AttendanceExceptionSaveDTO dto);

    void delete(Long id);
}
