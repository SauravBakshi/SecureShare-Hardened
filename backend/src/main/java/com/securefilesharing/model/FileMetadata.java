package com.securefilesharing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private Long fileSize;

    @JsonIgnore
    private String filePath;

    @Column(name = "encrypted_aes_key", length = 1024)
    @JsonIgnore
    private String encryptedAesKey;

    private Long ownerId;

    private LocalDateTime uploadedAt;

    @Builder.Default
    private String status = "ACTIVE";

    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
    }
}
