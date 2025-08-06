package com.recipehub.backendrecipehub.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String message;
    private UserResponseDTO user;
    
    public LoginResponseDTO(String message, UserResponseDTO user) {
        this.message = message;
        this.user = user;
    }
} 