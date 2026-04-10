package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.RoleSaveDTO;
import com.edusmart.manager.entity.EduRoleEntity;
import com.edusmart.manager.entity.EduRolePermissionEntity;
import com.edusmart.manager.mapper.EduRoleMapper;
import com.edusmart.manager.mapper.EduRolePermissionMapper;
import com.edusmart.manager.service.admin.AdminRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminRoleServiceImpl implements AdminRoleService {
    private final EduRoleMapper roleMapper;
    private final EduRolePermissionMapper rolePermissionMapper;

    public AdminRoleServiceImpl(EduRoleMapper roleMapper, EduRolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public PageData<EduRoleEntity> page(long current, long size, String keyword) {
        QueryWrapper<EduRoleEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("role_code", keyword).or().like("role_name", keyword));
        }
        wrapper.orderByDesc("id");
        Page<EduRoleEntity> page = roleMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public EduRoleEntity getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public Long create(RoleSaveDTO dto) {
        EduRoleEntity entity = new EduRoleEntity();
        entity.setRoleCode(dto.getRoleCode());
        entity.setRoleName(dto.getRoleName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        roleMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, RoleSaveDTO dto) {
        EduRoleEntity old = roleMapper.selectById(id);
        if (old == null) {
            return;
        }
        old.setRoleCode(dto.getRoleCode());
        old.setRoleName(dto.getRoleName());
        old.setDescription(dto.getDescription());
        old.setStatus(dto.getStatus() == null ? old.getStatus() : dto.getStatus());
        roleMapper.updateById(old);
    }

    @Override
    public void delete(Long id) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<EduRolePermissionEntity>().eq(EduRolePermissionEntity::getRoleId, id));
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<EduRolePermissionEntity>().eq(EduRolePermissionEntity::getRoleId, roleId));
        for (Long permissionId : permissionIds) {
            EduRolePermissionEntity rel = new EduRolePermissionEntity();
            rel.setRoleId(roleId);
            rel.setPermissionId(permissionId);
            rolePermissionMapper.insert(rel);
        }
    }
}
