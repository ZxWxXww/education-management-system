package com.edusmart.manager.dto.teacher;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TeacherScoreSaveDTO {
    @NotNull(message = "考试ID不能为空")
    private Long examId;
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    @NotNull(message = "分数不能为空")
    private BigDecimal score;
    private Integer rankInClass;
    private String teacherComment;
    private LocalDateTime publishTime;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Integer getRankInClass() { return rankInClass; }
    public void setRankInClass(Integer rankInClass) { this.rankInClass = rankInClass; }
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
}
