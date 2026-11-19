package com.securefilesharing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$", message = "Use letters, numbers, dots, underscores, or hyphens")
    private String username;
    
    @NotBlank
    @Email
    @Size(max = 254)
    private String email;
    
    @NotBlank
    @Size(min = 12, max = 72)
    private String password;
}
