package com.securefilesharing.controller;

import com.securefilesharing.dto.AdminStatisticsResponse;
import com.securefilesharing.repository.FileMetadataRepository;
import com.securefilesharing.repository.SecurityEventRepository;
import com.securefilesharing.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final SecurityEventRepository securityEventRepository;

    public AdminController(UserRepository userRepository, FileMetadataRepository fileMetadataRepository, SecurityEventRepository securityEventRepository) {
        this.userRepository = userRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.securityEventRepository = securityEventRepository;
    }

    @GetMapping("/statistics")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatisticsResponse> getStatistics() {
        AdminStatisticsResponse stats = AdminStatisticsResponse.builder()
                .totalUsers(userRepository.count())
                .totalFiles(fileMetadataRepository.count())
                .activeUsers(userRepository.count())
                .failedLoginCount(securityEventRepository.countByResult("FAILURE"))
                .securityEventsCount(securityEventRepository.count())
                .build();
        return ResponseEntity.ok(stats);
    }
}
