package com.recipehub.backendrecipehub.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PasswordUpdateDTO {
    
    @NotBlank(message = "Current password is required")
    private String currentPassword;
    
    @NotBlank(message = "New password is required")
    private String newPassword;
} 