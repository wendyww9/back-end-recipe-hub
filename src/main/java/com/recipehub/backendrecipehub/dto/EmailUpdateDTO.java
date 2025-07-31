package com.recipehub.backendrecipehub.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Data
public class EmailUpdateDTO {
    
    @NotBlank(message = "Current password is required")
    private String currentPassword;
    
    @NotBlank(message = "New email is required")
    @Email(message = "New email must be a valid email address")
    private String newEmail;
} 