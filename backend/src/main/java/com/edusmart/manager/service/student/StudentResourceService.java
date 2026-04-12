package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentResourcePageQueryDTO;
import com.edusmart.manager.dto.student.StudentResourcePageItemDTO;

public interface StudentResourceService {
    PageData<StudentResourcePageItemDTO> pageResources(StudentResourcePageQueryDTO queryDTO);
    StudentResourcePageItemDTO getResource(Long id);
}
