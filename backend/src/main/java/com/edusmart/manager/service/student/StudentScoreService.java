package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentExamScorePageQueryDTO;
import com.edusmart.manager.dto.student.StudentScorePageItemDTO;

public interface StudentScoreService {
    PageData<StudentScorePageItemDTO> pageScores(StudentExamScorePageQueryDTO queryDTO);
    StudentScorePageItemDTO getScore(Long id);
}
