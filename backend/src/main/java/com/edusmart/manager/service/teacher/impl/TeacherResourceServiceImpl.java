package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherResourcePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherResourcePageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherResourceSaveDTO;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduTeachingResourceEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduTeachingResourceMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.teacher.TeacherResourceService;
import com.edusmart.manager.service.teacher.TeacherScopeGuard;
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
public class TeacherResourceServiceImpl implements TeacherResourceService {
    private final EduTeachingResourceMapper resourceMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final CurrentUserService currentUserService;
    private final TeacherScopeGuard teacherScopeGuard;

    public TeacherResourceServiceImpl(
            EduTeachingResourceMapper resourceMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            CurrentUserService currentUserService,
            TeacherScopeGuard teacherScopeGuard
    ) {
        this.resourceMapper = resourceMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
        this.teacherScopeGuard = teacherScopeGuard;
    }

    @Override
    public PageData<TeacherResourcePageItemDTO> page(TeacherResourcePageQueryDTO queryDTO) {
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (teacherProfile == null) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }
        QueryWrapper<EduTeachingResourceEntity> w = new QueryWrapper<>();
        w.eq("teacher_id", teacherProfile.getId());
        if (StringUtils.hasText(queryDTO.getKeyword())) w.like("title", queryDTO.getKeyword());
        if (queryDTO.getClassId() != null) w.eq("class_id", queryDTO.getClassId());
        if (queryDTO.getCourseId() != null) w.eq("course_id", queryDTO.getCourseId());
        if (queryDTO.getStatus() != null) w.eq("status", queryDTO.getStatus());
        w.orderByDesc("id");
        Page<EduTeachingResourceEntity> p = resourceMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), buildItems(p.getRecords()));
    }

    @Override
    public TeacherResourcePageItemDTO getById(Long id) {
        EduTeachingResourceEntity resource = resourceMapper.selectById(id);
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (resource == null || teacherProfile == null || !Objects.equals(resource.getTeacherId(), teacherProfile.getId())) {
            throw new ResponseStatusException(NOT_FOUND, "资源不存在");
        }
        return buildItems(Collections.singletonList(resource)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "资源不存在"));
    }

    @Override
    public Long create(TeacherResourceSaveDTO dto) {
        EduTeacherProfileEntity teacherProfile = teacherScopeGuard.requireCurrentTeacherProfile();
        teacherScopeGuard.requireOwnedClass(dto.getClassId());
        EduTeachingResourceEntity e = new EduTeachingResourceEntity();
        fill(e, dto, teacherProfile.getId());
        e.setDownloadCount(0);
        resourceMapper.insert(e);
        return e.getId();
    }

    @Override
    public void update(Long id, TeacherResourceSaveDTO dto) {
        EduTeachingResourceEntity e = teacherScopeGuard.requireOwnedResource(id);
        EduTeacherProfileEntity teacherProfile = teacherScopeGuard.requireCurrentTeacherProfile();
        teacherScopeGuard.requireOwnedClass(dto.getClassId());
        fill(e, dto, teacherProfile.getId());
        resourceMapper.updateById(e);
    }

    @Override
    public void delete(Long id) {
        teacherScopeGuard.requireOwnedResource(id);
        resourceMapper.deleteById(id);
    }

    private void fill(EduTeachingResourceEntity e, TeacherResourceSaveDTO dto, Long teacherId) {
        e.setClassId(dto.getClassId());
        e.setCourseId(dto.getCourseId());
        e.setTeacherId(teacherId);
        e.setTitle(dto.getTitle());
        e.setCategory(dto.getCategory());
        e.setFileType(dto.getFileType());
        e.setFileName(dto.getFileName());
        e.setFileUrl(dto.getFileUrl());
        e.setFileSizeKb(dto.getFileSizeKb());
        e.setDescription(dto.getDescription());
        e.setStatus(dto.getStatus());
    }

    private EduTeacherProfileEntity getCurrentTeacherProfile() {
        Long currentUserId = currentUserService.getCurrentUserId();
        return teacherProfileMapper.selectOne(new QueryWrapper<EduTeacherProfileEntity>().eq("user_id", currentUserId));
    }

    private List<TeacherResourcePageItemDTO> buildItems(List<EduTeachingResourceEntity> resources) {
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
            TeacherResourcePageItemDTO item = new TeacherResourcePageItemDTO();
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
