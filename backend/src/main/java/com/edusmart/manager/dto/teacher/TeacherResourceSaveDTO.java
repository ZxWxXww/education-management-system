package com.edusmart.manager.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TeacherResourceSaveDTO {
    @NotNull(message = "班级ID不能为空")
    private Long classId;
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String category;
    @NotBlank(message = "文件类型不能为空")
    private String fileType;
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    @NotBlank(message = "文件地址不能为空")
    private String fileUrl;
    private Long fileSizeKb;
    private String description;
    private Integer status = 1;

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Long getFileSizeKb() { return fileSizeKb; }
    public void setFileSizeKb(Long fileSizeKb) { this.fileSizeKb = fileSizeKb; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
