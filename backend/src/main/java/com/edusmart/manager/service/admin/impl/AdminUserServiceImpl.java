package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminUserPageQueryDTO;
import com.edusmart.manager.dto.admin.AdminUserSaveDTO;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.entity.EduUserRoleEntity;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.mapper.EduUserRoleMapper;
import com.edusmart.manager.service.admin.AdminUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    private final EduUserMapper userMapper;
    private final EduUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(EduUserMapper userMapper, EduUserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageData<EduUserEntity> page(AdminUserPageQueryDTO queryDTO) {
        QueryWrapper<EduUserEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like("username", queryDTO.getKeyword())
                    .or().like("real_name", queryDTO.getKeyword())
                    .or().like("phone", queryDTO.getKeyword()));
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        if (queryDTO.getRoleId() != null) {
            wrapper.inSql("id", "select user_id from edu_user_role where role_id = " + queryDTO.getRoleId());
        }
        wrapper.orderByDesc("id");
        Page<EduUserEntity> page = userMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public EduUserEntity getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public Long create(AdminUserSaveDTO dto) {
        EduUserEntity entity = new EduUserEntity();
        entity.setUsername(dto.getUsername());
        entity.setRealName(dto.getRealName());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        entity.setPasswordHash(passwordEncoder.encode(StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "Admin@123456"));
        userMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, AdminUserSaveDTO dto) {
        EduUserEntity old = userMapper.selectById(id);
        if (old == null) {
            return;
        }
        old.setUsername(dto.getUsername());
        old.setRealName(dto.getRealName());
        old.setPhone(dto.getPhone());
        old.setEmail(dto.getEmail());
        old.setStatus(dto.getStatus() == null ? old.getStatus() : dto.getStatus());
        if (StringUtils.hasText(dto.getPassword())) {
            old.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        userMapper.updateById(old);
    }

    @Override
    public void delete(Long id) {
        userRoleMapper.delete(new LambdaQueryWrapper<EduUserRoleEntity>().eq(EduUserRoleEntity::getUserId, id));
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<EduUserRoleEntity>().eq(EduUserRoleEntity::getUserId, userId));
        for (Long roleId : roleIds) {
            EduUserRoleEntity rel = new EduUserRoleEntity();
            rel.setUserId(userId);
            rel.setRoleId(roleId);
            userRoleMapper.insert(rel);
        }
    }
}
