package com.edusmart.manager.controller;

import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.LoginRequest;
import com.edusmart.manager.dto.LoginResponse;
import com.edusmart.manager.dto.SwitchRoleRequest;
import com.edusmart.manager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        // 登录实际校验逻辑下沉到服务层，控制器仅负责协议编排。
        return Result.success(authService.login(req));
    }

    @PostMapping("/switch-role")
    public Result<LoginResponse> switchRole(@Valid @RequestBody SwitchRoleRequest req) {
        return Result.success(authService.switchRole(req));
    }
}

