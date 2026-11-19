package com.securefilesharing.service;

import com.securefilesharing.dto.FileUploadResponse;
import com.securefilesharing.model.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {
    FileUploadResponse uploadFile(MultipartFile file, Long ownerId);
    List<FileMetadata> getFilesByOwner(Long ownerId);
    FileMetadata getFileById(Long id);
    void deleteFile(Long id, Long requestingUserId);
    Resource downloadFile(Long id, Long requestingUserId);
}
