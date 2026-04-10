package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentResourcePageQueryDTO;
import com.edusmart.manager.entity.EduTeachingResourceEntity;
import com.edusmart.manager.mapper.EduTeachingResourceMapper;
import com.edusmart.manager.service.student.StudentResourceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StudentResourceServiceImpl implements StudentResourceService {
    private final EduTeachingResourceMapper resourceMapper;
    public StudentResourceServiceImpl(EduTeachingResourceMapper resourceMapper) { this.resourceMapper = resourceMapper; }

    @Override
    public PageData<EduTeachingResourceEntity> pageResources(StudentResourcePageQueryDTO queryDTO) {
        QueryWrapper<EduTeachingResourceEntity> w = new QueryWrapper<>();
        if (queryDTO.getClassId() != null) w.eq("class_id", queryDTO.getClassId());
        if (queryDTO.getCourseId() != null) w.eq("course_id", queryDTO.getCourseId());
        if (StringUtils.hasText(queryDTO.getKeyword())) w.like("title", queryDTO.getKeyword());
        if (queryDTO.getStatus() != null) w.eq("status", queryDTO.getStatus());
        w.orderByDesc("id");
        Page<EduTeachingResourceEntity> p = resourceMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduTeachingResourceEntity getResource(Long id) {
        return resourceMapper.selectById(id);
    }
}
