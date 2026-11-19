package com.securefilesharing.service;

import com.securefilesharing.dto.FileShareRequest;
import com.securefilesharing.dto.FileShareResponse;
import java.util.List;

public interface FileShareService {
    FileShareResponse shareFile(FileShareRequest request, Long ownerId);
    List<FileShareResponse> getSharesForUser(Long userId);
    void deleteShare(Long shareId, Long requestingUserId);
}
