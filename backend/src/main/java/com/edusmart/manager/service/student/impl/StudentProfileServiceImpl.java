package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edusmart.manager.dto.student.StudentPasswordUpdateDTO;
import com.edusmart.manager.dto.student.StudentProfileDetailDTO;
import com.edusmart.manager.dto.student.StudentProfileUpdateDTO;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.student.StudentProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public StudentProfileServiceImpl(
            EduStudentProfileMapper studentProfileMapper,
            EduUserMapper userMapper,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService
    ) {
        this.studentProfileMapper = studentProfileMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Override
    public StudentProfileDetailDTO getCurrentProfile() {
        EduUserEntity user = currentUserService.getCurrentUser();
        EduStudentProfileEntity profile = getCurrentProfileEntity();
        return buildProfileDetail(user, profile);
    }

    @Override
    public void updateProfile(StudentProfileUpdateDTO dto) {
        Long currentUserId = currentUserService.getCurrentUserId();
        EduUserEntity user = userMapper.selectById(currentUserId);
        if (user != null) {
            user.setRealName(dto.getRealName());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            userMapper.updateById(user);
        }
        EduStudentProfileEntity profile = getCurrentProfileEntity();
        if (profile != null) {
            profile.setGrade(dto.getGrade());
            profile.setClassNameText(dto.getClassName());
            profile.setGuardianName(dto.getGuardianName());
            profile.setGuardianPhone(dto.getGuardianPhone());
            profile.setAddress(dto.getAddress());
            profile.setIntro(dto.getIntro());
            studentProfileMapper.updateById(profile);
        }
    }

    @Override
    public void updatePassword(StudentPasswordUpdateDTO dto) {
        EduUserEntity user = userMapper.selectById(currentUserService.getCurrentUserId());
        if (user == null) {
            throw new ResponseStatusException(NOT_FOUND, "\u5b66\u751f\u8d26\u53f7\u4e0d\u5b58\u5728");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(BAD_REQUEST, "\u65e7\u5bc6\u7801\u4e0d\u6b63\u786e");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    private EduStudentProfileEntity getCurrentProfileEntity() {
        return studentProfileMapper.selectOne(new LambdaQueryWrapper<EduStudentProfileEntity>()
                .eq(EduStudentProfileEntity::getUserId, currentUserService.getCurrentUserId()));
    }

    private StudentProfileDetailDTO buildProfileDetail(EduUserEntity user, EduStudentProfileEntity profile) {
        StudentProfileDetailDTO detail = new StudentProfileDetailDTO();
        detail.setUserId(user.getId());
        detail.setRealName(user.getRealName());
        detail.setPhone(user.getPhone());
        detail.setEmail(user.getEmail());
        if (profile == null) {
            return detail;
        }
        detail.setStudentProfileId(profile.getId());
        detail.setStudentNo(profile.getStudentNo());
        detail.setGrade(profile.getGrade());
        detail.setClassName(profile.getClassNameText());
        detail.setGuardianName(profile.getGuardianName());
        detail.setGuardianPhone(profile.getGuardianPhone());
        detail.setAddress(profile.getAddress());
        detail.setIntro(profile.getIntro());
        detail.setCreatedAt(profile.getCreatedAt());
        detail.setUpdatedAt(profile.getUpdatedAt());
        return detail;
    }
}
