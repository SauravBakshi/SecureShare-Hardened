package com.securefilesharing.service.impl;

import com.securefilesharing.exception.FileStorageException;
import com.securefilesharing.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Primary
public class LocalStorageServiceImpl implements StorageService {

    private final Path fileStorageLocation;

    public LocalStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String customFileName) throws FileStorageException {
        try {
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null && originalFileName.contains(".") ? 
                originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            
            String uniqueFileName = customFileName != null ? customFileName : UUID.randomUUID().toString() + extension;
            Path targetLocation = safePath(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file. Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws FileStorageException {
        try {
            Path filePath = safePath(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileName) throws FileStorageException {
        try {
            Path filePath = safePath(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName, ex);
        }
    }

    @Override
    public Path getFileStoragePath() {
        return this.fileStorageLocation;
    }

    private Path safePath(String fileName) {
        Path path = this.fileStorageLocation.resolve(fileName).normalize();
        if (!path.startsWith(this.fileStorageLocation) || path.equals(this.fileStorageLocation)) {
            throw new FileStorageException("Invalid storage path");
        }
        return path;
    }
}
