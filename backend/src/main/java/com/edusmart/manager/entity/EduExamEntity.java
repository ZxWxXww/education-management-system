package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("edu_exam")
public class EduExamEntity extends BaseEntity {
    @TableField("exam_name")
    private String examName;
    @TableField("class_id")
    private Long classId;
    @TableField("course_id")
    private Long courseId;
    @TableField("teacher_id")
    private Long teacherId;
    @TableField("exam_time")
    private LocalDateTime examTime;
    @TableField("full_score")
    private BigDecimal fullScore;
    private String status;

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public LocalDateTime getExamTime() { return examTime; }
    public void setExamTime(LocalDateTime examTime) { this.examTime = examTime; }
    public BigDecimal getFullScore() { return fullScore; }
    public void setFullScore(BigDecimal fullScore) { this.fullScore = fullScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
