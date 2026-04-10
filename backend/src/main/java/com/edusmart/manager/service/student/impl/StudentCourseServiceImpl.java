package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentCoursePageQueryDTO;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.service.student.StudentCourseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StudentCourseServiceImpl implements StudentCourseService {
    private final EduClassMapper classMapper;
    private final EduClassStudentMapper classStudentMapper;

    public StudentCourseServiceImpl(EduClassMapper classMapper, EduClassStudentMapper classStudentMapper) {
        this.classMapper = classMapper;
        this.classStudentMapper = classStudentMapper;
    }

    @Override
    public PageData<EduClassEntity> pageCourses(StudentCoursePageQueryDTO queryDTO) {
        QueryWrapper<EduClassEntity> w = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            w.and(x -> x.like("class_name", queryDTO.getKeyword()).or().like("class_code", queryDTO.getKeyword()));
        }
        if (queryDTO.getCourseId() != null) {
            w.eq("course_id", queryDTO.getCourseId());
        }
        if (queryDTO.getStudentId() != null) {
            w.inSql("id", "select class_id from edu_class_student where student_id = " + queryDTO.getStudentId() + " and status = 1");
        }
        w.orderByDesc("id");
        Page<EduClassEntity> p = classMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduClassEntity getClassDetail(Long classId) {
        return classMapper.selectById(classId);
    }
}
