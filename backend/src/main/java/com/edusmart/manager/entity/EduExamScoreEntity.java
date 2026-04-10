package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("edu_exam_score")
public class EduExamScoreEntity extends BaseEntity {
    @TableField("exam_id")
    private Long examId;
    @TableField("student_id")
    private Long studentId;
    private BigDecimal score;
    @TableField("rank_in_class")
    private Integer rankInClass;
    @TableField("teacher_comment")
    private String teacherComment;
    @TableField("publish_time")
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
