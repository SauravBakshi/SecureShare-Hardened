package com.securefilesharing.repository;

import com.securefilesharing.model.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileShareRepository extends JpaRepository<FileShare, Long> {
    List<FileShare> findBySharedWithUserId(Long userId);
    List<FileShare> findByOwnerId(Long ownerId);
    void deleteByFileId(Long fileId);
    List<FileShare> findByFileId(Long fileId);
    Optional<FileShare> findByFileIdAndSharedWithUserId(Long fileId, Long userId);
}
