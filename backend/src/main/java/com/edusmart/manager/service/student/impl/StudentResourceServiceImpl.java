package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentResourcePageItemDTO;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduClassStudentEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.dto.student.StudentResourcePageQueryDTO;
import com.edusmart.manager.entity.EduTeachingResourceEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeachingResourceMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.student.StudentResourceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudentResourceServiceImpl implements StudentResourceService {
    private final EduTeachingResourceMapper resourceMapper;
    private final EduClassStudentMapper classStudentMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final CurrentUserService currentUserService;

    public StudentResourceServiceImpl(
            EduTeachingResourceMapper resourceMapper,
            EduClassStudentMapper classStudentMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            CurrentUserService currentUserService
    ) {
        this.resourceMapper = resourceMapper;
        this.classStudentMapper = classStudentMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public PageData<StudentResourcePageItemDTO> pageResources(StudentResourcePageQueryDTO queryDTO) {
        Long studentProfileId = getCurrentStudentProfileId();
        List<Long> classIds = classStudentMapper.selectList(new QueryWrapper<EduClassStudentEntity>()
                        .eq("student_id", studentProfileId)
                        .eq("status", 1))
                .stream()
                .map(EduClassStudentEntity::getClassId)
                .distinct()
                .collect(Collectors.toList());
        if (classIds.isEmpty()) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }
        QueryWrapper<EduTeachingResourceEntity> w = new QueryWrapper<>();
        w.in("class_id", classIds);
        if (queryDTO.getClassId() != null) w.eq("class_id", queryDTO.getClassId());
        if (queryDTO.getCourseId() != null) w.eq("course_id", queryDTO.getCourseId());
        if (StringUtils.hasText(queryDTO.getKeyword())) w.like("title", queryDTO.getKeyword());
        if (queryDTO.getStatus() != null) w.eq("status", queryDTO.getStatus());
        w.orderByDesc("id");
        Page<EduTeachingResourceEntity> p = resourceMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), buildItems(p.getRecords()));
    }

    @Override
    public StudentResourcePageItemDTO getResource(Long id) {
        EduTeachingResourceEntity resource = resourceMapper.selectById(id);
        Long studentProfileId = getCurrentStudentProfileId();
        if (resource == null || !isStudentInClass(studentProfileId, resource.getClassId())) {
            throw new ResponseStatusException(NOT_FOUND, "资源不存在");
        }
        return buildItems(Collections.singletonList(resource)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "资源不存在"));
    }

    private Long getCurrentStudentProfileId() {
        EduStudentProfileEntity profile = studentProfileMapper.selectOne(
                new QueryWrapper<EduStudentProfileEntity>().eq("user_id", currentUserService.getCurrentUserId())
        );
        if (profile == null) {
            throw new ResponseStatusException(NOT_FOUND, "学生档案不存在");
        }
        return profile.getId();
    }

    private boolean isStudentInClass(Long studentProfileId, Long classId) {
        return classStudentMapper.selectCount(new QueryWrapper<EduClassStudentEntity>()
                .eq("student_id", studentProfileId)
                .eq("class_id", classId)
                .eq("status", 1)) > 0;
    }

    private List<StudentResourcePageItemDTO> buildItems(List<EduTeachingResourceEntity> resources) {
        if (resources == null || resources.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(
                        resources.stream().map(EduTeachingResourceEntity::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduCourseEntity> courseMap = courseMapper.selectBatchIds(
                        resources.stream().map(EduTeachingResourceEntity::getCourseId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherProfileMapper.selectBatchIds(
                        resources.stream().map(EduTeachingResourceEntity::getTeacherId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduUserEntity> userMap = userMapper.selectBatchIds(
                        teacherMap.values().stream().map(EduTeacherProfileEntity::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right));

        return resources.stream().map(resource -> {
            StudentResourcePageItemDTO item = new StudentResourcePageItemDTO();
            item.setId(resource.getId());
            item.setClassId(resource.getClassId());
            item.setCourseId(resource.getCourseId());
            item.setTeacherId(resource.getTeacherId());
            item.setTitle(resource.getTitle());
            item.setCategory(resource.getCategory());
            item.setFileType(resource.getFileType());
            item.setFileName(resource.getFileName());
            item.setFileUrl(resource.getFileUrl());
            item.setFileSizeKb(resource.getFileSizeKb());
            item.setDescription(resource.getDescription());
            item.setDownloadCount(resource.getDownloadCount());
            item.setStatus(resource.getStatus());
            item.setCreatedAt(resource.getCreatedAt());
            item.setUpdatedAt(resource.getUpdatedAt());
            EduClassEntity clazz = classMap.get(resource.getClassId());
            EduCourseEntity course = courseMap.get(resource.getCourseId());
            EduTeacherProfileEntity teacherProfile = teacherMap.get(resource.getTeacherId());
            EduUserEntity teacherUser = teacherProfile == null ? null : userMap.get(teacherProfile.getUserId());
            item.setClassName(clazz == null ? "" : clazz.getClassName());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            return item;
        }).collect(Collectors.toList());
    }
}
