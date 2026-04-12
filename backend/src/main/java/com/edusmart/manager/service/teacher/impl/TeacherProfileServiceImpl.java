package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edusmart.manager.dto.teacher.TeacherPasswordUpdateDTO;
import com.edusmart.manager.dto.teacher.TeacherProfileDetailDTO;
import com.edusmart.manager.dto.teacher.TeacherProfileUpdateDTO;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.teacher.TeacherProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TeacherProfileServiceImpl implements TeacherProfileService {
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public TeacherProfileServiceImpl(
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService
    ) {
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Override
    public TeacherProfileDetailDTO getCurrentProfileDetail() {
        return getProfileDetailByUserId(currentUserService.getCurrentUserId());
    }

    @Override
    public TeacherProfileDetailDTO getProfileDetailByUserId(Long userId) {
        EduUserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResponseStatusException(NOT_FOUND, "教师账号不存在");
        }
        EduTeacherProfileEntity profile = teacherProfileMapper.selectOne(new LambdaQueryWrapper<EduTeacherProfileEntity>()
                .eq(EduTeacherProfileEntity::getUserId, userId)
                .last("limit 1"));
        return buildProfileDetail(user, profile);
    }

    @Override
    public void updateProfile(TeacherProfileUpdateDTO dto) {
        Long currentUserId = currentUserService.getCurrentUserId();
        EduUserEntity user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new ResponseStatusException(NOT_FOUND, "教师账号不存在");
        }
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        userMapper.updateById(user);

        EduTeacherProfileEntity profile = teacherProfileMapper.selectOne(new LambdaQueryWrapper<EduTeacherProfileEntity>()
                .eq(EduTeacherProfileEntity::getUserId, currentUserId)
                .last("limit 1"));
        if (profile == null) {
            throw new ResponseStatusException(NOT_FOUND, "教师档案不存在");
        }
        profile.setTitle(dto.getTitle());
        profile.setSubject(dto.getSubject());
        profile.setIntro(dto.getIntro());
        profile.setHireDate(dto.getHireDate());
        teacherProfileMapper.updateById(profile);
    }

    @Override
    public void updatePassword(TeacherPasswordUpdateDTO dto) {
        EduUserEntity user = userMapper.selectById(currentUserService.getCurrentUserId());
        if (user == null) {
            throw new ResponseStatusException(NOT_FOUND, "教师账号不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(BAD_REQUEST, "旧密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    private TeacherProfileDetailDTO buildProfileDetail(EduUserEntity user, EduTeacherProfileEntity profile) {
        TeacherProfileDetailDTO detail = new TeacherProfileDetailDTO();
        detail.setUserId(user.getId());
        detail.setUsername(user.getUsername());
        detail.setRealName(user.getRealName());
        detail.setPhone(user.getPhone());
        detail.setEmail(user.getEmail());
        detail.setAvatarUrl(user.getAvatarUrl());
        if (profile == null) {
            return detail;
        }
        detail.setTeacherProfileId(profile.getId());
        detail.setTeacherNo(profile.getTeacherNo());
        detail.setTitle(profile.getTitle());
        detail.setSubject(profile.getSubject());
        detail.setIntro(profile.getIntro());
        detail.setHireDate(profile.getHireDate());
        detail.setCreatedAt(profile.getCreatedAt());
        detail.setUpdatedAt(profile.getUpdatedAt());
        return detail;
    }
}
