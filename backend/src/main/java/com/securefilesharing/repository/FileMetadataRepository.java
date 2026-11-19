package com.securefilesharing.repository;

import com.securefilesharing.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByOwnerId(Long ownerId);
    List<FileMetadata> findByOwnerIdAndStatus(Long ownerId, String status);
    @Query("select coalesce(sum(f.fileSize), 0) from FileMetadata f where f.ownerId = :ownerId and f.status = 'ACTIVE'")
    Long totalActiveBytesByOwner(@Param("ownerId") Long ownerId);
}
