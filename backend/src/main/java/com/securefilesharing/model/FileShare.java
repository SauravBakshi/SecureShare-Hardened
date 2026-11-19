package com.securefilesharing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;
    
    private Long ownerId;
    
    private Long sharedWithUserId;
    
    @Builder.Default
    private String permission = "READ";
    
    private LocalDateTime sharedAt;

    @PrePersist
    public void prePersist() {
        this.sharedAt = LocalDateTime.now();
    }
}
