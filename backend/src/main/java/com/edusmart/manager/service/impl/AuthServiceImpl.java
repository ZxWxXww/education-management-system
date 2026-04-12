package com.edusmart.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edusmart.manager.dto.LoginRequest;
import com.edusmart.manager.dto.LoginResponse;
import com.edusmart.manager.dto.SwitchRoleRequest;
import com.edusmart.manager.entity.EduPermissionEntity;
import com.edusmart.manager.entity.EduRoleEntity;
import com.edusmart.manager.entity.EduRolePermissionEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.entity.EduUserRoleEntity;
import com.edusmart.manager.mapper.EduPermissionMapper;
import com.edusmart.manager.mapper.EduRoleMapper;
import com.edusmart.manager.mapper.EduRolePermissionMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.mapper.EduUserRoleMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.security.JwtTokenService;
import com.edusmart.manager.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthServiceImpl implements AuthService {
    private final EduUserMapper userMapper;
    private final EduUserRoleMapper userRoleMapper;
    private final EduRoleMapper roleMapper;
    private final EduRolePermissionMapper rolePermissionMapper;
    private final EduPermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CurrentUserService currentUserService;

    public AuthServiceImpl(EduUserMapper userMapper,
                           EduUserRoleMapper userRoleMapper,
                           EduRoleMapper roleMapper,
                           EduRolePermissionMapper rolePermissionMapper,
                           EduPermissionMapper permissionMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenService jwtTokenService,
                           CurrentUserService currentUserService) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.currentUserService = currentUserService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = safeTrim(request.getUsername());
        String password = request.getPassword();
        String requestedRole = normalizeRole(request.getRole());

        // 先校验用户基础账号信息，确保登录主体真实存在。
        EduUserEntity user = userMapper.selectOne(new LambdaQueryWrapper<EduUserEntity>()
                .eq(EduUserEntity::getUsername, username));
        if (user == null || !StringUtils.hasText(user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "账号或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "账号或密码错误");
        }
        if (!Objects.equals(user.getStatus(), 1)) {
            throw new ResponseStatusException(UNAUTHORIZED, "账号已被禁用");
        }

        return buildSession(user, requestedRole, true);
    }

    @Override
    public LoginResponse switchRole(SwitchRoleRequest request) {
        EduUserEntity user = currentUserService.getCurrentUser();
        if (!Objects.equals(user.getStatus(), 1)) {
            throw new ResponseStatusException(UNAUTHORIZED, "账号已被禁用");
        }
        return buildSession(user, normalizeRole(request.getRole()), false);
    }

    private LoginResponse buildSession(EduUserEntity user, String requestedRole, boolean updateLastLoginAt) {
        // 再加载该用户实际拥有的角色，避免前端通过 role 字段伪造身份。
        List<EduUserRoleEntity> userRoleRelations = userRoleMapper.selectList(new LambdaQueryWrapper<EduUserRoleEntity>()
                .eq(EduUserRoleEntity::getUserId, user.getId()));
        if (userRoleRelations.isEmpty()) {
            throw new ResponseStatusException(UNAUTHORIZED, "账号未分配角色");
        }

        List<Long> roleIds = userRoleRelations.stream()
                .map(EduUserRoleEntity::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<EduRoleEntity> roles = roleIds.isEmpty() ? Collections.emptyList() : roleMapper.selectBatchIds(roleIds);
        if (roles.isEmpty()) {
            throw new ResponseStatusException(UNAUTHORIZED, "账号角色数据不存在");
        }

        Map<Long, EduRoleEntity> roleMap = roles.stream()
                .collect(Collectors.toMap(EduRoleEntity::getId, role -> role, (left, right) -> left, LinkedHashMap::new));
        List<EduRoleEntity> orderedRoles = new ArrayList<>();
        for (Long roleId : roleIds) {
            EduRoleEntity role = roleMap.get(roleId);
            if (role != null) {
                orderedRoles.add(role);
            }
        }

        EduRoleEntity matchedRole = orderedRoles.stream()
                .filter(role -> normalizeRole(role.getRoleCode()).equals(requestedRole))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "角色不匹配"));
        if (!Objects.equals(matchedRole.getStatus(), 1)) {
            throw new ResponseStatusException(UNAUTHORIZED, "角色已被禁用");
        }

        List<EduRoleEntity> enabledRoles = orderedRoles.stream()
                .filter(Objects::nonNull)
                .filter(role -> Objects.equals(role.getStatus(), 1))
                .filter(role -> StringUtils.hasText(role.getRoleCode()))
                .toList();
        if (enabledRoles.isEmpty()) {
            throw new ResponseStatusException(UNAUTHORIZED, "账号无可用角色");
        }

        EduRoleEntity activeRole = enabledRoles.stream()
                .filter(role -> normalizeRole(role.getRoleCode()).equals(requestedRole))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "角色不匹配"));

        // 权限只返回当前登录角色对应的数据，避免把其他角色权限混入当前会话。
        List<EduPermissionEntity> permissions = loadPermissions(List.of(activeRole.getId()));
        if (updateLastLoginAt) {
            LocalDateTime now = LocalDateTime.now();
            user.setLastLoginAt(now);
            userMapper.updateById(user);
        }

        List<String> roleCodes = enabledRoles.stream()
                .map(EduRoleEntity::getRoleCode)
                .filter(StringUtils::hasText)
                .map(this::normalizeRole)
                .distinct()
                .toList();
        List<String> permissionCodes = permissions.stream()
                .map(EduPermissionEntity::getPermCode)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        String token = jwtTokenService.createToken(
                user.getUsername(),
                normalizeRole(activeRole.getRoleCode()),
                roleCodes,
                permissionCodes
        );
        return new LoginResponse(
                token,
                normalizeRole(activeRole.getRoleCode()),
                buildUserInfo(user),
                buildRoleInfos(enabledRoles),
                buildPermissionInfos(permissions)
        );
    }

    private List<EduPermissionEntity> loadPermissions(List<Long> roleIds) {
        List<EduRolePermissionEntity> rolePermissions = rolePermissionMapper.selectList(new LambdaQueryWrapper<EduRolePermissionEntity>()
                .in(EduRolePermissionEntity::getRoleId, roleIds));
        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> permissionIds = rolePermissions.stream()
                .map(EduRolePermissionEntity::getPermissionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (permissionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<EduPermissionEntity> permissions = permissionMapper.selectBatchIds(permissionIds);
        if (permissions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, EduPermissionEntity> permissionMap = permissions.stream()
                .collect(Collectors.toMap(EduPermissionEntity::getId, permission -> permission, (left, right) -> left, LinkedHashMap::new));
        List<EduPermissionEntity> orderedPermissions = new ArrayList<>();
        for (Long permissionId : permissionIds) {
            EduPermissionEntity permission = permissionMap.get(permissionId);
            if (permission != null) {
                orderedPermissions.add(permission);
            }
        }
        return orderedPermissions;
    }

    private LoginResponse.UserInfo buildUserInfo(EduUserEntity user) {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setStatus(user.getStatus());
        userInfo.setLastLoginAt(user.getLastLoginAt());
        return userInfo;
    }

    private List<LoginResponse.RoleInfo> buildRoleInfos(List<EduRoleEntity> roles) {
        return roles.stream().map(role -> {
            LoginResponse.RoleInfo roleInfo = new LoginResponse.RoleInfo();
            roleInfo.setId(role.getId());
            roleInfo.setRoleCode(role.getRoleCode());
            roleInfo.setRoleName(role.getRoleName());
            return roleInfo;
        }).toList();
    }

    private List<LoginResponse.PermissionInfo> buildPermissionInfos(List<EduPermissionEntity> permissions) {
        return permissions.stream().map(permission -> {
            LoginResponse.PermissionInfo permissionInfo = new LoginResponse.PermissionInfo();
            permissionInfo.setId(permission.getId());
            permissionInfo.setPermCode(permission.getPermCode());
            permissionInfo.setPermName(permission.getPermName());
            permissionInfo.setPermType(permission.getPermType());
            permissionInfo.setResourcePath(permission.getResourcePath());
            permissionInfo.setHttpMethod(permission.getHttpMethod());
            return permissionInfo;
        }).toList();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            throw new ResponseStatusException(UNAUTHORIZED, "角色不能为空");
        }
        return role.trim().toLowerCase();
    }
}
