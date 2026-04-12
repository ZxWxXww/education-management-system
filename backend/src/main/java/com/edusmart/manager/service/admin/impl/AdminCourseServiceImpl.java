package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.StatusLabelMapper;
import com.edusmart.manager.dto.admin.AdminCoursePageItemDTO;
import com.edusmart.manager.dto.admin.CoursePageQueryDTO;
import com.edusmart.manager.dto.admin.CourseSaveDTO;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.service.admin.AdminCourseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminCourseServiceImpl implements AdminCourseService {
    private final EduCourseMapper courseMapper;

    public AdminCourseServiceImpl(EduCourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @Override
    public PageData<AdminCoursePageItemDTO> page(CoursePageQueryDTO queryDTO) {
        QueryWrapper<EduCourseEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like("course_code", queryDTO.getKeyword())
                    .or().like("course_name", queryDTO.getKeyword())
                    .or().like("subject", queryDTO.getKeyword()));
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        wrapper.orderByDesc("id");
        Page<EduCourseEntity> page = courseMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords().stream().map(this::toDto).toList());
    }

    @Override
    public AdminCoursePageItemDTO getById(Long id) {
        return toDto(courseMapper.selectById(id));
    }

    @Override
    public Long create(CourseSaveDTO dto) {
        EduCourseEntity entity = new EduCourseEntity();
        entity.setCourseCode(dto.getCourseCode());
        entity.setCourseName(dto.getCourseName());
        entity.setSubject(dto.getSubject());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        courseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, CourseSaveDTO dto) {
        EduCourseEntity old = courseMapper.selectById(id);
        if (old == null) {
            return;
        }
        old.setCourseCode(dto.getCourseCode());
        old.setCourseName(dto.getCourseName());
        old.setSubject(dto.getSubject());
        old.setDescription(dto.getDescription());
        old.setStatus(dto.getStatus() == null ? old.getStatus() : dto.getStatus());
        courseMapper.updateById(old);
    }

    @Override
    public void delete(Long id) {
        courseMapper.deleteById(id);
    }

    private AdminCoursePageItemDTO toDto(EduCourseEntity entity) {
        if (entity == null) {
            return null;
        }
        AdminCoursePageItemDTO dto = new AdminCoursePageItemDTO();
        dto.setId(entity.getId());
        dto.setCourseCode(entity.getCourseCode());
        dto.setCourseName(entity.getCourseName());
        dto.setSubject(entity.getSubject());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setStatusLabel(StatusLabelMapper.courseStatusLabel(entity.getStatus()));
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
