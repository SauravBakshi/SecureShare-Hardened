package com.securefilesharing.service.impl;

import com.securefilesharing.model.SecurityEvent;
import com.securefilesharing.repository.SecurityEventRepository;
import com.securefilesharing.service.SecurityEventService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SecurityEventServiceImpl implements SecurityEventService {

    private final SecurityEventRepository securityEventRepository;

    public SecurityEventServiceImpl(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    @Override
    public SecurityEvent logEvent(Long userId, String action, String ipAddress, String result, String riskLevel) {
        SecurityEvent event = SecurityEvent.builder()
                .userId(userId)
                .action(action)
                .ipAddress(ipAddress)
                .result(result)
                .riskLevel(riskLevel)
                .build();
        return securityEventRepository.save(event);
    }

    @Override
    public List<SecurityEvent> getAllEvents() {
        return securityEventRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @Override
    public List<SecurityEvent> getEventsByUser(Long userId) {
        return securityEventRepository.findByUserId(userId);
    }
}
