package com.edusmart.manager.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduUserMapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class CurrentUserService {
    private final EduUserMapper userMapper;

    public CurrentUserService(EduUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(UNAUTHORIZED, "未登录或登录已失效");
        }
        return String.valueOf(authentication.getPrincipal());
    }

    public EduUserEntity getCurrentUser() {
        String username = getCurrentUsername();
        EduUserEntity user = userMapper.selectOne(new LambdaQueryWrapper<EduUserEntity>()
                .eq(EduUserEntity::getUsername, username)
                .eq(EduUserEntity::getStatus, 1)
                .last("limit 1"));
        if (user == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "当前登录用户不存在或已被禁用");
        }
        return user;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
