package com.securefilesharing.controller;

import com.securefilesharing.model.SecurityEvent;
import com.securefilesharing.service.SecurityEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.securefilesharing.security.CurrentUser;

@RestController
@RequestMapping("/api/security-events")
public class SecurityEventController {
    private final SecurityEventService securityEventService;

    public SecurityEventController(SecurityEventService securityEventService) {
        this.securityEventService = securityEventService;
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityEvent>> getAllEvents() {
        return ResponseEntity.ok(securityEventService.getAllEvents());
    }

    @GetMapping("/me")
    public ResponseEntity<List<SecurityEvent>> getMyEvents() {
        return ResponseEntity.ok(securityEventService.getEventsByUser(CurrentUser.id()));
    }
}
