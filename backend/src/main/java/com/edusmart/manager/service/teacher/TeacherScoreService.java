package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherScorePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherScorePageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherScoreSaveDTO;

import java.util.Map;

public interface TeacherScoreService {
    PageData<TeacherScorePageItemDTO> page(TeacherScorePageQueryDTO queryDTO);
    TeacherScorePageItemDTO getById(Long id);
    Long create(TeacherScoreSaveDTO dto);
    void update(Long id, TeacherScoreSaveDTO dto);
    void delete(Long id);
    Map<String, Object> analysis(Long examId);
}
