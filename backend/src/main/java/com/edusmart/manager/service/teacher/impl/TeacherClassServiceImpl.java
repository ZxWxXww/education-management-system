package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherClassPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherClassSaveDTO;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.service.teacher.TeacherClassService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TeacherClassServiceImpl implements TeacherClassService {
    private final EduClassMapper classMapper;
    public TeacherClassServiceImpl(EduClassMapper classMapper) { this.classMapper = classMapper; }

    @Override
    public PageData<EduClassEntity> page(TeacherClassPageQueryDTO queryDTO) {
        QueryWrapper<EduClassEntity> w = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            w.and(x -> x.like("class_code", queryDTO.getKeyword()).or().like("class_name", queryDTO.getKeyword()));
        }
        if (queryDTO.getCourseId() != null) w.eq("course_id", queryDTO.getCourseId());
        if (queryDTO.getStatus() != null) w.eq("status", queryDTO.getStatus());
        w.orderByDesc("id");
        Page<EduClassEntity> page = classMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public EduClassEntity getById(Long id) { return classMapper.selectById(id); }

    @Override
    public Long create(TeacherClassSaveDTO dto) {
        EduClassEntity e = new EduClassEntity();
        e.setClassCode(dto.getClassCode());
        e.setClassName(dto.getClassName());
        e.setCourseId(dto.getCourseId());
        e.setStartDate(dto.getStartDate());
        e.setEndDate(dto.getEndDate());
        e.setStatus(dto.getStatus());
        classMapper.insert(e);
        return e.getId();
    }

    @Override
    public void update(Long id, TeacherClassSaveDTO dto) {
        EduClassEntity e = classMapper.selectById(id);
        if (e == null) return;
        e.setClassCode(dto.getClassCode());
        e.setClassName(dto.getClassName());
        e.setCourseId(dto.getCourseId());
        e.setStartDate(dto.getStartDate());
        e.setEndDate(dto.getEndDate());
        e.setStatus(dto.getStatus());
        classMapper.updateById(e);
    }

    @Override
    public void delete(Long id) { classMapper.deleteById(id); }
}
