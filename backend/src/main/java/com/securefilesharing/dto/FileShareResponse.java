package com.securefilesharing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileShareResponse {
    private Long id;
    private Long fileId;
    private String fileName;
    private Long ownerId;
    private String ownerUsername;
    private Long sharedWithUserId;
    private String sharedWithUsername;
    private String permission;
    private LocalDateTime sharedAt;
}
