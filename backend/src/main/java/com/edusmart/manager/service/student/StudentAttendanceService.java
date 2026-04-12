package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentAttendanceExceptionPageItemDTO;
import com.edusmart.manager.dto.student.StudentAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAttendancePageItemDTO;
import com.edusmart.manager.dto.student.StudentAttendancePageQueryDTO;

public interface StudentAttendanceService {
    PageData<StudentAttendancePageItemDTO> pageAttendance(StudentAttendancePageQueryDTO queryDTO);

    StudentAttendancePageItemDTO getAttendance(Long id);

    PageData<StudentAttendanceExceptionPageItemDTO> pageExceptions(StudentAttendanceExceptionPageQueryDTO queryDTO);

    StudentAttendanceExceptionPageItemDTO getException(Long id);
}
