package com.edusmart.manager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("edu_teaching_resource")
public class EduTeachingResourceEntity extends BaseEntity {
    @TableField("class_id")
    private Long classId;
    @TableField("course_id")
    private Long courseId;
    @TableField("teacher_id")
    private Long teacherId;
    private String title;
    private String category;
    @TableField("file_type")
    private String fileType;
    @TableField("file_name")
    private String fileName;
    @TableField("file_url")
    private String fileUrl;
    @TableField("file_size_kb")
    private Long fileSizeKb;
    private String description;
    @TableField("download_count")
    private Integer downloadCount;
    private Integer status;

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
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
