package com.securefilesharing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatisticsResponse {
    private long totalUsers;
    private long totalFiles;
    private long activeUsers;
    private long failedLoginCount;
    private long securityEventsCount;
}
