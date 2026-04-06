package com.edusmart.manager.controller;

import com.edusmart.manager.common.ApiResponse;
import com.edusmart.manager.dto.LoginRequest;
import com.edusmart.manager.dto.LoginResponse;
import com.edusmart.manager.security.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    private final String defaultPassword;
    private final Map<String, String> userPasswordHash;

    public AuthController(JwtTokenService jwtTokenService, PasswordEncoder passwordEncoder) {
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = System.getenv().getOrDefault("EDU_DEFAULT_PASSWORD", "ChangeMe123!");
        this.userPasswordHash = Map.of(
                "admin", passwordEncoder.encode(defaultPassword),
                "teacher", passwordEncoder.encode(defaultPassword),
                "student", passwordEncoder.encode(defaultPassword)
        );
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        String username = req.getUsername().trim();
        String role = normalizeRole(req.getRole());

        String storedHash = userPasswordHash.get(username);
        if (storedHash == null || !passwordEncoder.matches(req.getPassword(), storedHash)) {
            throw new ResponseStatusException(UNAUTHORIZED, "账号或密码错误");
        }

        if (!username.equals(role)) {
            throw new ResponseStatusException(UNAUTHORIZED, "角色不匹配");
        }

        String token = jwtTokenService.createToken(username, role.toUpperCase());
        return ApiResponse.success(new LoginResponse(token, role));
    }

    private String normalizeRole(String role) {
        if (role == null) throw new ResponseStatusException(UNAUTHORIZED, "角色不能为空");
        String r = role.trim().toLowerCase();
        if (!r.equals("admin") && !r.equals("teacher") && !r.equals("student")) {
            throw new ResponseStatusException(UNAUTHORIZED, "角色非法");
        }
        return r;
    }
}

