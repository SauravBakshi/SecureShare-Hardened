package com.securefilesharing.repository;

import com.securefilesharing.model.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    List<SecurityEvent> findByUserId(Long userId);
    List<SecurityEvent> findByRiskLevel(String riskLevel);
    long countByResult(String result);
}
