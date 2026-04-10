package com.edusmart.manager.dto.student;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class StudentExamScorePageQueryDTO extends BasePageQueryDTO {
    private Long studentId;
    private Long examId;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
}
