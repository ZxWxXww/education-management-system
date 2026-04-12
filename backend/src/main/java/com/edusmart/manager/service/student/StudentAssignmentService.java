package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentAssignmentPageItemDTO;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionSaveDTO;

public interface StudentAssignmentService {
    PageData<StudentAssignmentPageItemDTO> pageSubmissions(StudentAssignmentSubmissionPageQueryDTO queryDTO);
    StudentAssignmentPageItemDTO getSubmission(Long id);
    Long createSubmission(StudentAssignmentSubmissionSaveDTO dto);
    void updateSubmission(Long id, StudentAssignmentSubmissionSaveDTO dto);
    void deleteSubmission(Long id);
}
