package com.edusmart.manager.service;

import com.edusmart.manager.dto.LoginRequest;
import com.edusmart.manager.dto.LoginResponse;
import com.edusmart.manager.dto.SwitchRoleRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse switchRole(SwitchRoleRequest request);
}
