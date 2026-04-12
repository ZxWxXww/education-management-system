package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminRolePageItemDTO;
import com.edusmart.manager.dto.admin.RoleSaveDTO;
import com.edusmart.manager.entity.EduPermissionEntity;
import com.edusmart.manager.entity.EduRoleEntity;
import com.edusmart.manager.entity.EduRolePermissionEntity;
import com.edusmart.manager.mapper.EduPermissionMapper;
import com.edusmart.manager.mapper.EduRoleMapper;
import com.edusmart.manager.mapper.EduRolePermissionMapper;
import com.edusmart.manager.service.admin.AdminRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminRoleServiceImpl implements AdminRoleService {
    private final EduRoleMapper roleMapper;
    private final EduRolePermissionMapper rolePermissionMapper;
    private final EduPermissionMapper permissionMapper;

    public AdminRoleServiceImpl(
            EduRoleMapper roleMapper,
            EduRolePermissionMapper rolePermissionMapper,
            EduPermissionMapper permissionMapper
    ) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public PageData<AdminRolePageItemDTO> page(long current, long size, String keyword) {
        QueryWrapper<EduRoleEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("role_code", keyword).or().like("role_name", keyword));
        }
        wrapper.orderByDesc("id");
        Page<EduRoleEntity> page = roleMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildRoleItems(page.getRecords()));
    }

    @Override
    public AdminRolePageItemDTO getById(Long id) {
        EduRoleEntity role = roleMapper.selectById(id);
        if (role == null) {
            return null;
        }
        return buildRoleItems(List.of(role)).stream().findFirst().orElse(null);
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

    private List<AdminRolePageItemDTO> buildRoleItems(List<EduRoleEntity> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = roles.stream().map(EduRoleEntity::getId).toList();
        List<EduRolePermissionEntity> relations = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<EduRolePermissionEntity>().in(EduRolePermissionEntity::getRoleId, roleIds)
        );

        List<Long> permissionIds = relations.stream()
                .map(EduRolePermissionEntity::getPermissionId)
                .distinct()
                .toList();

        Map<Long, EduPermissionEntity> permissionMap = new HashMap<>();
        if (!permissionIds.isEmpty()) {
            permissionMap = permissionMapper.selectBatchIds(permissionIds).stream()
                    .collect(Collectors.toMap(EduPermissionEntity::getId, item -> item));
        }

        Map<Long, List<EduRolePermissionEntity>> relationMap = relations.stream()
                .collect(Collectors.groupingBy(EduRolePermissionEntity::getRoleId));

        List<AdminRolePageItemDTO> result = new ArrayList<>();
        for (EduRoleEntity role : roles) {
            AdminRolePageItemDTO item = new AdminRolePageItemDTO();
            item.setId(role.getId());
            item.setRoleCode(role.getRoleCode());
            item.setRoleName(role.getRoleName());
            item.setDescription(role.getDescription());
            item.setStatus(role.getStatus());

            List<EduRolePermissionEntity> currentRelations = relationMap.getOrDefault(role.getId(), List.of());
            List<Long> currentPermissionIds = new ArrayList<>();
            List<String> permissionNames = new ArrayList<>();
            for (EduRolePermissionEntity relation : currentRelations) {
                currentPermissionIds.add(relation.getPermissionId());
                EduPermissionEntity permission = permissionMap.get(relation.getPermissionId());
                if (permission != null) {
                    permissionNames.add(permission.getPermName());
                }
            }
            item.setPermissionIds(currentPermissionIds);
            item.setPermissionNames(permissionNames);
            result.add(item);
        }

        return result;
    }
}
