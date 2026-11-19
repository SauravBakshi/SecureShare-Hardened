package com.securefilesharing.service.impl;

import com.securefilesharing.dto.FileUploadResponse;
import com.securefilesharing.exception.BadRequestException;
import com.securefilesharing.exception.ResourceNotFoundException;
import com.securefilesharing.model.FileMetadata;
import com.securefilesharing.model.User;
import com.securefilesharing.repository.FileMetadataRepository;
import com.securefilesharing.repository.UserRepository;
import com.securefilesharing.repository.FileShareRepository;
import com.securefilesharing.service.EncryptionService;
import com.securefilesharing.service.FileService;
import com.securefilesharing.service.RsaEncryptionService;
import com.securefilesharing.service.StorageService;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.security.SecureRandom;

@Service
public class FileServiceImpl implements FileService {
    private static final long USER_STORAGE_QUOTA = 250L * 1024 * 1024;

    private final FileMetadataRepository fileMetadataRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final RsaEncryptionService rsaEncryptionService;
    private final FileShareRepository fileShareRepository;

    public FileServiceImpl(
            FileMetadataRepository fileMetadataRepository,
            StorageService storageService,
            UserRepository userRepository,
            EncryptionService encryptionService,
            RsaEncryptionService rsaEncryptionService,
            FileShareRepository fileShareRepository) {

        this.fileMetadataRepository = fileMetadataRepository;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.rsaEncryptionService = rsaEncryptionService;
        this.fileShareRepository = fileShareRepository;
    }

    @Override
    public FileUploadResponse uploadFile(
            MultipartFile file,
            Long ownerId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + ownerId));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "File cannot be empty");
        }
        long used = fileMetadataRepository.totalActiveBytesByOwner(ownerId);
        if (file.getSize() > USER_STORAGE_QUOTA - used) {
            throw new BadRequestException("Your account storage quota has been reached");
        }

        Path storageDirectory =
                storageService.getFileStoragePath();

        String encryptedFileName =
                UUID.randomUUID().toString() + ".enc";

        Path encryptedFilePath =
                storageDirectory.resolve(encryptedFileName);

        Path temporaryFile = null;

        try {

            /*
             * STEP 1:
             * Save uploaded file temporarily.
             */
            temporaryFile = Files.createTempFile(
                    storageDirectory,
                    "upload-",
                    ".tmp");

            Files.copy(
                    file.getInputStream(),
                    temporaryFile,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            /*
             * STEP 2:
             * Encrypt the file using AES-256-GCM.
             */
            byte[] aesKey = new byte[32];
            new SecureRandom().nextBytes(aesKey);
            String encryptedAesKey;
            try {
                encryptionService.encryptFile(temporaryFile, encryptedFilePath, aesKey);
                encryptedAesKey = rsaEncryptionService.encryptAesKey(aesKey);
            } finally {
                java.util.Arrays.fill(aesKey, (byte) 0);
            }

            /*
             * STEP 5:
             * Delete plaintext temporary file.
             */
            Files.deleteIfExists(temporaryFile);

            temporaryFile = null;

            /*
             * STEP 6:
             * Save file metadata and RSA-encrypted
             * AES key in MySQL.
             */
            FileMetadata metadata =
                    FileMetadata.builder()
                            .fileName(sanitizeFileName(file.getOriginalFilename()))
                            .fileSize(file.getSize())
                            .filePath(encryptedFileName)
                            .encryptedAesKey(encryptedAesKey)
                            .ownerId(owner.getId())
                            .status("ACTIVE")
                            .build();

            FileMetadata savedMetadata =
                    fileMetadataRepository.save(metadata);

            /*
             * STEP 7:
             * Return upload response.
             */
            return FileUploadResponse.builder()
                    .id(savedMetadata.getId())
                    .fileName(savedMetadata.getFileName())
                    .fileSize(savedMetadata.getFileSize())
                    .uploadedAt(savedMetadata.getUploadedAt())
                    .status(savedMetadata.getStatus())
                    .message(
                            "File encrypted and uploaded successfully")
                    .build();

        } catch (Exception ex) {

            /*
             * Cleanup if something goes wrong.
             */
            try {

                if (temporaryFile != null) {
                    Files.deleteIfExists(temporaryFile);
                }

                Files.deleteIfExists(
                        encryptedFilePath);

            } catch (Exception ignored) {

                // Do not hide original error.
            }

            throw new BadRequestException(
                    "Could not encrypt and upload file");
        }
    }

    @Override
    public List<FileMetadata> getFilesByOwner(
            Long ownerId) {

        return fileMetadataRepository
                .findByOwnerIdAndStatus(
                        ownerId,
                        "ACTIVE");
    }

    @Override
    public FileMetadata getFileById(Long id) {
        FileMetadata metadata = fileMetadataRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "File not found with id: " + id));
        Long requesterId = com.securefilesharing.security.CurrentUser.id();
        boolean owner = metadata.getOwnerId().equals(requesterId);
        boolean recipient = fileShareRepository.findByFileIdAndSharedWithUserId(id, requesterId).isPresent();
        if (!owner && !recipient) throw new BadRequestException("You do not have permission to access this file");
        if (!"ACTIVE".equals(metadata.getStatus())) throw new ResourceNotFoundException("File is not available");
        return metadata;
    }

    @Override
    public void deleteFile(
            Long id,
            Long requestingUserId) {

        FileMetadata metadata =
                getFileById(id);

        if (!metadata.getOwnerId().equals(requestingUserId)) {

            throw new BadRequestException(
                    "You don't have permission to delete this file");
        }

        storageService.deleteFile(
                metadata.getFilePath());

        fileShareRepository.deleteByFileId(id);

        metadata.setStatus("DELETED");

        fileMetadataRepository.save(metadata);
    }

    @Override
    public Resource downloadFile(Long id, Long requestingUserId) {

        FileMetadata metadata =
                getFileById(id);

        if (!metadata.getOwnerId().equals(requestingUserId)
                && fileShareRepository.findByFileIdAndSharedWithUserId(id, requestingUserId).isEmpty()) {
            throw new BadRequestException("You do not have permission to download this file");
        }

        Path storageRoot = storageService.getFileStoragePath().toAbsolutePath().normalize();
        Path encryptedFile = storageRoot.resolve(metadata.getFilePath()).normalize();
        if (!encryptedFile.startsWith(storageRoot)) throw new BadRequestException("Invalid stored file path");
        if (!"ACTIVE".equals(metadata.getStatus())) throw new ResourceNotFoundException("File is not available");

        if (!Files.exists(encryptedFile)) {

            throw new ResourceNotFoundException(
                    "Encrypted file not found");
        }

        try {

            /*
             * Create temporary decrypted file.
             */
            Path decryptedFile = Files.createTempFile("sfs-decrypted-", ".tmp");
            try {

            /*
             * Decrypt using AES-256-GCM.
             */
                byte[] fileKey = rsaEncryptionService.decryptAesKey(metadata.getEncryptedAesKey());
                try { encryptionService.decryptFile(encryptedFile, decryptedFile, fileKey); }
                finally { java.util.Arrays.fill(fileKey, (byte) 0); }

                return new DeletingFileResource(decryptedFile);
            } catch (Exception ex) {
                Files.deleteIfExists(decryptedFile);
                throw ex;
            }

        } catch (Exception ex) {

            throw new BadRequestException(
                    "Could not decrypt file");
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "download";
        String clean = java.nio.file.Paths.get(name.replace('\\', '/')).getFileName().toString()
                .replaceAll("[\\p{Cntrl}]", "_").replaceAll("[\\r\\n\\\"]", "_").trim();
        return clean.isBlank() ? "download" : clean.substring(0, Math.min(clean.length(), 180));
    }
}
