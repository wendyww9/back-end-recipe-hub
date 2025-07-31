package com.recipehub.backendrecipehub.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
} 