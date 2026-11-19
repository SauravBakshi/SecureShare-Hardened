package com.securefilesharing.service;

import com.securefilesharing.exception.FileStorageException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

public interface StorageService {
    String storeFile(MultipartFile file, String customFileName) throws FileStorageException;
    Resource loadFileAsResource(String fileName) throws FileStorageException;
    void deleteFile(String fileName) throws FileStorageException;
    Path getFileStoragePath();
}
