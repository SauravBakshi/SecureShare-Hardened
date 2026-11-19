package com.securefilesharing.service.impl;

import com.securefilesharing.dto.AuthResponse;
import com.securefilesharing.dto.LoginRequest;
import com.securefilesharing.dto.RegisterRequest;
import com.securefilesharing.exception.BadRequestException;
import com.securefilesharing.model.SecurityEvent;
import com.securefilesharing.model.User;
import com.securefilesharing.repository.SecurityEventRepository;
import com.securefilesharing.repository.UserRepository;
import com.securefilesharing.service.AuthService;
import com.securefilesharing.security.TokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SecurityEventRepository securityEventRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final String dummyPasswordHash;

    public AuthServiceImpl(
            UserRepository userRepository,
            SecurityEventRepository securityEventRepository,
            TokenService tokenService) {

        this.userRepository = userRepository;
        this.securityEventRepository = securityEventRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.dummyPasswordHash = this.passwordEncoder.encode("not-a-real-user-password");
        this.tokenService = tokenService;
    }

    @Override
    public AuthResponse register(RegisterRequest request, String ipAddress) {

        if (request.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("Password must be at most 72 UTF-8 bytes");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        // Hash the password before storing it
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(hashedPassword)
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);

        logSecurityEvent(
                savedUser.getId(),
                "REGISTER",
                "SUCCESS",
                "LOW", ipAddress);

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .message("User registered successfully")
                .token(tokenService.issue(savedUser.getId(), savedUser.getRole()))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request, String ipAddress) {

        if (request.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            logSecurityEvent(null, "LOGIN_FAILURE", "FAILURE", "MEDIUM", ipAddress);
            throw new BadRequestException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    passwordEncoder.matches(request.getPassword(), dummyPasswordHash);
                    logSecurityEvent(
                            null,
                            "LOGIN_FAILURE",
                            "FAILURE",
                            "MEDIUM", ipAddress);

                    return new BadRequestException(
                            "Invalid username or password");
                });

        // Verify the entered password against the BCrypt hash
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            logSecurityEvent(
                    user.getId(),
                    "LOGIN_FAILURE",
                    "FAILURE",
                    "MEDIUM", ipAddress);

            throw new BadRequestException(
                    "Invalid username or password");
        }

        logSecurityEvent(
                user.getId(),
                "LOGIN_SUCCESS",
                "SUCCESS",
                "LOW", ipAddress);

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Login successful")
                .token(tokenService.issue(user.getId(), user.getRole()))
                .build();
    }

    private void logSecurityEvent(
            Long userId,
            String action,
            String result,
            String riskLevel,
            String ipAddress) {

        SecurityEvent event = SecurityEvent.builder()
                .userId(userId)
                .action(action)
                .ipAddress(ipAddress)
                .result(result)
                .riskLevel(riskLevel)
                .build();

        securityEventRepository.save(event);
    }
}
