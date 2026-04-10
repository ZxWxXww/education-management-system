package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherResourcePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherResourceSaveDTO;
import com.edusmart.manager.entity.EduTeachingResourceEntity;
import com.edusmart.manager.mapper.EduTeachingResourceMapper;
import com.edusmart.manager.service.teacher.TeacherResourceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TeacherResourceServiceImpl implements TeacherResourceService {
    private final EduTeachingResourceMapper resourceMapper;
    public TeacherResourceServiceImpl(EduTeachingResourceMapper resourceMapper) { this.resourceMapper = resourceMapper; }

    @Override
    public PageData<EduTeachingResourceEntity> page(TeacherResourcePageQueryDTO queryDTO) {
        QueryWrapper<EduTeachingResourceEntity> w = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) w.like("title", queryDTO.getKeyword());
        if (queryDTO.getClassId() != null) w.eq("class_id", queryDTO.getClassId());
        if (queryDTO.getCourseId() != null) w.eq("course_id", queryDTO.getCourseId());
        if (queryDTO.getStatus() != null) w.eq("status", queryDTO.getStatus());
        w.orderByDesc("id");
        Page<EduTeachingResourceEntity> p = resourceMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduTeachingResourceEntity getById(Long id) { return resourceMapper.selectById(id); }

    @Override
    public Long create(TeacherResourceSaveDTO dto) {
        EduTeachingResourceEntity e = new EduTeachingResourceEntity();
        fill(e, dto);
        e.setDownloadCount(0);
        resourceMapper.insert(e);
        return e.getId();
    }

    @Override
    public void update(Long id, TeacherResourceSaveDTO dto) {
        EduTeachingResourceEntity e = resourceMapper.selectById(id);
        if (e == null) return;
        fill(e, dto);
        resourceMapper.updateById(e);
    }

    @Override
    public void delete(Long id) { resourceMapper.deleteById(id); }

    private void fill(EduTeachingResourceEntity e, TeacherResourceSaveDTO dto) {
        e.setClassId(dto.getClassId());
        e.setCourseId(dto.getCourseId());
        e.setTeacherId(dto.getTeacherId());
        e.setTitle(dto.getTitle());
        e.setCategory(dto.getCategory());
        e.setFileType(dto.getFileType());
        e.setFileName(dto.getFileName());
        e.setFileUrl(dto.getFileUrl());
        e.setFileSizeKb(dto.getFileSizeKb());
        e.setDescription(dto.getDescription());
        e.setStatus(dto.getStatus());
    }
}
