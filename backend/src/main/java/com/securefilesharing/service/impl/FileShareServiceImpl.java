package com.securefilesharing.service.impl;

import com.securefilesharing.dto.FileShareRequest;
import com.securefilesharing.dto.FileShareResponse;
import com.securefilesharing.exception.BadRequestException;
import com.securefilesharing.exception.ResourceNotFoundException;
import com.securefilesharing.model.FileMetadata;
import com.securefilesharing.model.FileShare;
import com.securefilesharing.model.User;
import com.securefilesharing.repository.FileMetadataRepository;
import com.securefilesharing.repository.FileShareRepository;
import com.securefilesharing.repository.UserRepository;
import com.securefilesharing.service.FileShareService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileShareServiceImpl implements FileShareService {

    private final FileShareRepository fileShareRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;

    public FileShareServiceImpl(FileShareRepository fileShareRepository, FileMetadataRepository fileMetadataRepository, UserRepository userRepository) {
        this.fileShareRepository = fileShareRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.userRepository = userRepository;
    }

    @Override
    public FileShareResponse shareFile(FileShareRequest request, Long requestingUserId) {
        FileMetadata file = fileMetadataRepository.findById(request.getFileId())
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
                
        User sharedWith = userRepository.findById(request.getSharedWithUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User to share with not found"));
                
        User owner = userRepository.findById(file.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("File owner not found"));

        if (!file.getOwnerId().equals(requestingUserId) || !"ACTIVE".equals(file.getStatus())) {
            throw new BadRequestException("Only the active file owner can share this file");
        }
        if (requestingUserId.equals(sharedWith.getId())) throw new BadRequestException("A file cannot be shared with its owner");
        if (!"READ".equals(request.getPermission())) {
            throw new BadRequestException("Only READ shares are supported");
        }

        if (fileShareRepository.findByFileIdAndSharedWithUserId(file.getId(), sharedWith.getId()).isPresent()) {
            throw new BadRequestException("File is already shared with this user");
        }

        FileShare share = FileShare.builder()
                .fileId(file.getId())
                .ownerId(file.getOwnerId())
                .sharedWithUserId(sharedWith.getId())
                .permission(request.getPermission())
                .build();
                
        FileShare savedShare = fileShareRepository.save(share);

        return FileShareResponse.builder()
                .id(savedShare.getId())
                .fileId(file.getId())
                .fileName(file.getFileName())
                .ownerId(owner.getId())
                .ownerUsername(owner.getUsername())
                .sharedWithUserId(sharedWith.getId())
                .sharedWithUsername(sharedWith.getUsername())
                .permission(savedShare.getPermission())
                .sharedAt(savedShare.getSharedAt())
                .build();
    }

    @Override
    public List<FileShareResponse> getSharesForUser(Long userId) {
        List<FileShare> shares = fileShareRepository.findBySharedWithUserId(userId);
        
        return shares.stream().map(share -> {
            FileMetadata file = fileMetadataRepository.findById(share.getFileId()).orElse(null);
            User owner = userRepository.findById(share.getOwnerId()).orElse(null);
            User sharedWith = userRepository.findById(share.getSharedWithUserId()).orElse(null);
            
            return FileShareResponse.builder()
                    .id(share.getId())
                    .fileId(share.getFileId())
                    .fileName(file != null ? file.getFileName() : "Unknown")
                    .ownerId(share.getOwnerId())
                    .ownerUsername(owner != null ? owner.getUsername() : "Unknown")
                    .sharedWithUserId(share.getSharedWithUserId())
                    .sharedWithUsername(sharedWith != null ? sharedWith.getUsername() : "Unknown")
                    .permission(share.getPermission())
                    .sharedAt(share.getSharedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteShare(Long shareId, Long requestingUserId) {
        FileShare share = fileShareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found with id: " + shareId));
        if (!share.getOwnerId().equals(requestingUserId)) throw new BadRequestException("Only the owner can revoke this share");
        fileShareRepository.delete(share);
    }
}
