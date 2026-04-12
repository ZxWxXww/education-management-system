package com.edusmart.manager.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edusmart.manager.entity.EduRoleEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.entity.EduUserRoleEntity;
import com.edusmart.manager.mapper.EduRoleMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.mapper.EduUserRoleMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;
    private final EduUserMapper userMapper;
    private final EduUserRoleMapper userRoleMapper;
    private final EduRoleMapper roleMapper;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   EduUserMapper userMapper,
                                   EduUserRoleMapper userRoleMapper,
                                   EduRoleMapper roleMapper) {
        this.jwtTokenService = jwtTokenService;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtTokenService.parseClaims(token);
                String username = claims.getSubject();
                String role = normalizeRole(claims.get("role"));
                if (isActiveSession(username, role)) {
                    var authorities = resolveAuthorities(role, claims.get("permissions"));
                    var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> resolveAuthorities(String role, Object permissionClaim) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (role != null && !role.isBlank() && !"null".equalsIgnoreCase(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase(Locale.ROOT)));
        }
        if (permissionClaim instanceof List<?> permissionList) {
            permissionList.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return authorities;
    }

    private boolean isActiveSession(String username, String activeRole) {
        if (username == null || username.isBlank() || activeRole == null || activeRole.isBlank()) {
            return false;
        }

        EduUserEntity user = userMapper.selectOne(new LambdaQueryWrapper<EduUserEntity>()
                .eq(EduUserEntity::getUsername, username)
                .eq(EduUserEntity::getStatus, 1)
                .last("limit 1"));
        if (user == null) {
            return false;
        }

        List<EduUserRoleEntity> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<EduUserRoleEntity>()
                .eq(EduUserRoleEntity::getUserId, user.getId()));
        if (userRoles.isEmpty()) {
            return false;
        }

        List<Long> roleIds = userRoles.stream()
                .map(EduUserRoleEntity::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return false;
        }

        return roleMapper.selectBatchIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> Objects.equals(role.getStatus(), 1))
                .map(EduRoleEntity::getRoleCode)
                .map(this::normalizeRole)
                .anyMatch(activeRole::equals);
    }

    private String normalizeRole(Object role) {
        if (role == null) {
            return "";
        }
        return String.valueOf(role).trim().toLowerCase(Locale.ROOT);
    }
}

