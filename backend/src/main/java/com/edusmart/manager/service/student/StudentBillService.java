package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentBillPageItemDTO;
import com.edusmart.manager.dto.student.StudentBillPageQueryDTO;
import com.edusmart.manager.dto.student.StudentHourPackageSummaryDTO;

public interface StudentBillService {
    PageData<StudentBillPageItemDTO> page(StudentBillPageQueryDTO queryDTO);
    StudentBillPageItemDTO getById(Long id);
    StudentHourPackageSummaryDTO getHourPackageSummary();
}
