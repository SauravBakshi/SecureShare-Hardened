package com.securefilesharing.controller;

import com.securefilesharing.dto.FileShareRequest;
import com.securefilesharing.dto.FileShareResponse;
import com.securefilesharing.service.FileShareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.securefilesharing.security.CurrentUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/shares")
public class FileShareController {
    private final FileShareService fileShareService;

    public FileShareController(FileShareService fileShareService) {
        this.fileShareService = fileShareService;
    }

    @PostMapping
    public ResponseEntity<FileShareResponse> shareFile(@Valid @RequestBody FileShareRequest request) {
        FileShareResponse response = fileShareService.shareFile(request, CurrentUser.id());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    public ResponseEntity<List<FileShareResponse>> getSharesForUser() {
        return ResponseEntity.ok(fileShareService.getSharesForUser(CurrentUser.id()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShare(@PathVariable Long id) {
        fileShareService.deleteShare(id, CurrentUser.id());
        return ResponseEntity.ok().build();
    }
}
