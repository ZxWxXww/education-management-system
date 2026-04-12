package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminUserPageItemDTO;
import com.edusmart.manager.dto.admin.AdminUserPageQueryDTO;
import com.edusmart.manager.dto.admin.AdminUserSaveDTO;
import com.edusmart.manager.entity.EduRoleEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.entity.EduUserRoleEntity;
import com.edusmart.manager.mapper.EduRoleMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.mapper.EduUserRoleMapper;
import com.edusmart.manager.service.admin.AdminUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    private final EduUserMapper userMapper;
    private final EduUserRoleMapper userRoleMapper;
    private final EduRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(
            EduUserMapper userMapper,
            EduUserRoleMapper userRoleMapper,
            EduRoleMapper roleMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageData<AdminUserPageItemDTO> page(AdminUserPageQueryDTO queryDTO) {
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
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildUserPageItems(page.getRecords()));
    }

    private List<AdminUserPageItemDTO> buildUserPageItems(List<EduUserEntity> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = users.stream().map(EduUserEntity::getId).toList();
        List<EduUserRoleEntity> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<EduUserRoleEntity>().in(EduUserRoleEntity::getUserId, userIds)
        );

        List<Long> roleIds = userRoles.stream()
                .map(EduUserRoleEntity::getRoleId)
                .distinct()
                .toList();

        Map<Long, EduRoleEntity> roleMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            roleMap = roleMapper.selectBatchIds(roleIds).stream()
                    .collect(Collectors.toMap(EduRoleEntity::getId, item -> item));
        }

        Map<Long, List<EduUserRoleEntity>> userRoleMap = userRoles.stream()
                .collect(Collectors.groupingBy(EduUserRoleEntity::getUserId));

        List<AdminUserPageItemDTO> result = new ArrayList<>();
        for (EduUserEntity user : users) {
            AdminUserPageItemDTO item = new AdminUserPageItemDTO();
            item.setId(user.getId());
            item.setUsername(user.getUsername());
            item.setRealName(user.getRealName());
            item.setPhone(user.getPhone());
            item.setEmail(user.getEmail());
            item.setStatus(user.getStatus());
            item.setCreatedAt(user.getCreatedAt());
            item.setUpdatedAt(user.getUpdatedAt());
            item.setLastLoginAt(user.getLastLoginAt());

            List<EduUserRoleEntity> relations = userRoleMap.getOrDefault(user.getId(), List.of());
            List<Long> itemRoleIds = new ArrayList<>();
            List<String> roleCodes = new ArrayList<>();
            List<String> roleNames = new ArrayList<>();
            for (EduUserRoleEntity relation : relations) {
                itemRoleIds.add(relation.getRoleId());
                EduRoleEntity role = roleMap.get(relation.getRoleId());
                if (role != null) {
                    roleCodes.add(role.getRoleCode());
                    roleNames.add(role.getRoleName());
                }
            }
            item.setRoleIds(itemRoleIds);
            item.setRoleCodes(roleCodes);
            item.setRoleNames(roleNames);
            result.add(item);
        }

        return result;
    }

    @Override
    public AdminUserPageItemDTO getById(Long id) {
        EduUserEntity user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        return buildUserPageItems(List.of(user)).stream().findFirst().orElse(null);
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
