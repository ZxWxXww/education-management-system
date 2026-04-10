package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edusmart.manager.dto.student.StudentPasswordUpdateDTO;
import com.edusmart.manager.dto.student.StudentProfileUpdateDTO;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.service.student.StudentProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public StudentProfileServiceImpl(EduStudentProfileMapper studentProfileMapper, EduUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.studentProfileMapper = studentProfileMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EduStudentProfileEntity getProfileByUserId(Long userId) {
        return studentProfileMapper.selectOne(new LambdaQueryWrapper<EduStudentProfileEntity>().eq(EduStudentProfileEntity::getUserId, userId));
    }

    @Override
    public void updateProfile(StudentProfileUpdateDTO dto) {
        EduUserEntity user = userMapper.selectById(dto.getUserId());
        if (user != null) {
            user.setRealName(dto.getRealName());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            userMapper.updateById(user);
        }
        EduStudentProfileEntity profile = studentProfileMapper.selectOne(
                new LambdaQueryWrapper<EduStudentProfileEntity>().eq(EduStudentProfileEntity::getUserId, dto.getUserId())
        );
        if (profile != null) {
            profile.setAddress(dto.getAddress());
            profile.setIntro(dto.getIntro());
            studentProfileMapper.updateById(profile);
        }
    }

    @Override
    public void updatePassword(StudentPasswordUpdateDTO dto) {
        EduUserEntity user = userMapper.selectById(dto.getUserId());
        if (user == null) return;
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) return;
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}
