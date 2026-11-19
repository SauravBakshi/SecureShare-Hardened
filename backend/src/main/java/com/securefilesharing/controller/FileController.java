package com.securefilesharing.controller;

import com.securefilesharing.dto.FileUploadResponse;
import com.securefilesharing.model.FileMetadata;
import com.securefilesharing.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.securefilesharing.security.CurrentUser;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadFile(file, CurrentUser.id());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> getFilesByOwner() {
        return ResponseEntity.ok(fileService.getFilesByOwner(CurrentUser.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileMetadata> getFileById(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getFileById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id, CurrentUser.id());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        FileMetadata metadata = fileService.getFileById(id);
        Resource resource = fileService.downloadFile(id, CurrentUser.id());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, org.springframework.http.ContentDisposition.attachment()
                        .filename(metadata.getFileName() == null ? "download" : metadata.getFileName(), java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .body(resource);
    }
}
