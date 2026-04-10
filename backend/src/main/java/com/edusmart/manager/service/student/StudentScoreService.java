package com.edusmart.manager.service.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentExamScorePageQueryDTO;
import com.edusmart.manager.entity.EduExamScoreEntity;

public interface StudentScoreService {
    PageData<EduExamScoreEntity> pageScores(StudentExamScorePageQueryDTO queryDTO);
    EduExamScoreEntity getScore(Long id);
}
