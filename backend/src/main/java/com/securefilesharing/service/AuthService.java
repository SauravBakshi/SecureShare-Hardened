package com.securefilesharing.service;

import com.securefilesharing.dto.AuthResponse;
import com.securefilesharing.dto.LoginRequest;
import com.securefilesharing.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request, String ipAddress);
    AuthResponse login(LoginRequest request, String ipAddress);
}
