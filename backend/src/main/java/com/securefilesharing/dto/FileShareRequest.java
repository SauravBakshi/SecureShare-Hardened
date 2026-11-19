package com.securefilesharing.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
public class FileShareRequest {
    @NotNull
    private Long fileId;
    @NotNull
    private Long sharedWithUserId;
    @Pattern(regexp = "READ")
    private String permission = "READ";
}
