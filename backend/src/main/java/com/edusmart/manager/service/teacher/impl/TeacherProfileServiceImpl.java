package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edusmart.manager.dto.teacher.TeacherPasswordUpdateDTO;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.service.teacher.TeacherProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TeacherProfileServiceImpl implements TeacherProfileService {
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public TeacherProfileServiceImpl(EduTeacherProfileMapper teacherProfileMapper, EduUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EduTeacherProfileEntity getByUserId(Long userId) {
        return teacherProfileMapper.selectOne(new LambdaQueryWrapper<EduTeacherProfileEntity>().eq(EduTeacherProfileEntity::getUserId, userId));
    }

    @Override
    public void updatePassword(TeacherPasswordUpdateDTO dto) {
        EduUserEntity user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            return;
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            return;
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}
