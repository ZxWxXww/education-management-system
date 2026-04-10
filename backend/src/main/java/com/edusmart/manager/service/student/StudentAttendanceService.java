package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAttendancePageQueryDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;

public interface StudentAttendanceService {
    PageData<EduAttendanceRecordEntity> pageAttendance(StudentAttendancePageQueryDTO queryDTO);
    EduAttendanceRecordEntity getAttendance(Long id);
    PageData<EduAttendanceExceptionEntity> pageExceptions(StudentAttendanceExceptionPageQueryDTO queryDTO);
    EduAttendanceExceptionEntity getException(Long id);
}
