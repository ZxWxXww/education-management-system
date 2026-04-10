package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAssignmentSubmissionSaveDTO;
import com.edusmart.manager.entity.EduAssignmentSubmissionEntity;

public interface StudentAssignmentService {
    PageData<EduAssignmentSubmissionEntity> pageSubmissions(StudentAssignmentSubmissionPageQueryDTO queryDTO);
    EduAssignmentSubmissionEntity getSubmission(Long id);
    Long createSubmission(StudentAssignmentSubmissionSaveDTO dto);
    void updateSubmission(Long id, StudentAssignmentSubmissionSaveDTO dto);
    void deleteSubmission(Long id);
}
