package com.recipehub.backendrecipehub.mapper;

import com.recipehub.backendrecipehub.dto.UserRequestDTO;
import com.recipehub.backendrecipehub.dto.UserResponseDTO;
import com.recipehub.backendrecipehub.model.User;

public class UserMapper {
    
    public static User toEntity(UserRequestDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // Will be hashed in service
        return user;
    }
    
    public static UserResponseDTO toResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
} 