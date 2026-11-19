package com.securefilesharing.service;

import com.securefilesharing.model.SecurityEvent;
import java.util.List;

public interface SecurityEventService {
    SecurityEvent logEvent(Long userId, String action, String ipAddress, String result, String riskLevel);
    List<SecurityEvent> getAllEvents();
    List<SecurityEvent> getEventsByUser(Long userId);
}
