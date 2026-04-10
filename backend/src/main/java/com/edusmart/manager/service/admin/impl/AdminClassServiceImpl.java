package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.ClassPageQueryDTO;
import com.edusmart.manager.dto.admin.ClassSaveDTO;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.service.admin.AdminClassService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminClassServiceImpl implements AdminClassService {
    private final EduClassMapper classMapper;

    public AdminClassServiceImpl(EduClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    @Override
    public PageData<EduClassEntity> page(ClassPageQueryDTO queryDTO) {
        QueryWrapper<EduClassEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like("class_code", queryDTO.getKeyword())
                    .or().like("class_name", queryDTO.getKeyword()));
        }
        if (queryDTO.getCourseId() != null) {
            wrapper.eq("course_id", queryDTO.getCourseId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        wrapper.orderByDesc("id");
        Page<EduClassEntity> page = classMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public EduClassEntity getById(Long id) {
        return classMapper.selectById(id);
    }

    @Override
    public Long create(ClassSaveDTO dto) {
        EduClassEntity entity = new EduClassEntity();
        entity.setClassCode(dto.getClassCode());
        entity.setClassName(dto.getClassName());
        entity.setCourseId(dto.getCourseId());
        entity.setHeadTeacherId(dto.getHeadTeacherId());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        classMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, ClassSaveDTO dto) {
        EduClassEntity old = classMapper.selectById(id);
        if (old == null) {
            return;
        }
        old.setClassCode(dto.getClassCode());
        old.setClassName(dto.getClassName());
        old.setCourseId(dto.getCourseId());
        old.setHeadTeacherId(dto.getHeadTeacherId());
        old.setStartDate(dto.getStartDate());
        old.setEndDate(dto.getEndDate());
        old.setStatus(dto.getStatus() == null ? old.getStatus() : dto.getStatus());
        classMapper.updateById(old);
    }

    @Override
    public void delete(Long id) {
        classMapper.deleteById(id);
    }
}
