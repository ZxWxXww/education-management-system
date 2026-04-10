package com.edusmart.manager.dto.teacher;

import com.edusmart.manager.dto.BasePageQueryDTO;

public class TeacherScorePageQueryDTO extends BasePageQueryDTO {
    private Long examId;
    private Long studentId;
    private String keyword;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
