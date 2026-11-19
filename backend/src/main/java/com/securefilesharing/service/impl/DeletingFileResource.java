package com.securefilesharing.service.impl;

import org.springframework.core.io.AbstractResource;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

final class DeletingFileResource extends AbstractResource {
    private final Path path;
    DeletingFileResource(Path path) { this.path = path; }
    @Override public String getDescription() { return "temporary decrypted download"; }
    @Override public InputStream getInputStream() throws IOException {
        InputStream delegate = Files.newInputStream(path);
        return new FilterInputStream(delegate) {
            @Override public void close() throws IOException {
                try { super.close(); } finally { Files.deleteIfExists(path); }
            }
        };
    }
}
