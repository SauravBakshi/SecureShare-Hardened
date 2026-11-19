package com.securefilesharing.controller;

import com.securefilesharing.dto.ApiResponse;
import com.securefilesharing.dto.AuthResponse;
import com.securefilesharing.dto.LoginRequest;
import com.securefilesharing.dto.RegisterRequest;
import com.securefilesharing.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.securefilesharing.security.LoginRateLimiter;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AuthService authService, LoginRateLimiter loginRateLimiter) {
        this.authService = authService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        // IP could be used for logging, left as example
        String ipAddress = servletRequest.getRemoteAddr();
        AuthResponse response = authService.register(request, ipAddress);
        return new ResponseEntity<>(ApiResponse.ok("Registration successful", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String ipAddress = servletRequest.getRemoteAddr();
        if (!loginRateLimiter.allow(ipAddress)) return ResponseEntity.status(429).body(ApiResponse.<AuthResponse>error("Too many login attempts. Try again shortly."));
        AuthResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }
}
