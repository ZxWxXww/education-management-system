package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminAttendanceExceptionPageItemDTO;
import com.edusmart.manager.dto.admin.AttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.admin.AttendanceExceptionSaveDTO;

public interface AdminAttendanceExceptionService {
    PageData<AdminAttendanceExceptionPageItemDTO> page(AttendanceExceptionPageQueryDTO queryDTO);

    AdminAttendanceExceptionPageItemDTO getById(Long id);

    Long create(AttendanceExceptionSaveDTO dto);

    void update(Long id, AttendanceExceptionSaveDTO dto);

    void delete(Long id);
}
