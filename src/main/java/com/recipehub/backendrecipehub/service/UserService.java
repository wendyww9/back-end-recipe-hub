package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.UserRequestDTO;
import com.recipehub.backendrecipehub.dto.UserResponseDTO;
import com.recipehub.backendrecipehub.dto.UserUpdateDTO;
import com.recipehub.backendrecipehub.mapper.UserMapper;
import com.recipehub.backendrecipehub.exception.DuplicateResourceException;
import com.recipehub.backendrecipehub.exception.UserNotFoundException;
import com.recipehub.backendrecipehub.exception.ValidationException;
import com.recipehub.backendrecipehub.exception.InvalidCredentialsException;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO) {
        // Check if username already exists
        if (userRepository.findByUsername(userRequestDTO.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username", userRequestDTO.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(userRequestDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email", userRequestDTO.getEmail());
        }
        
        // Convert DTO to User entity
        User user = UserMapper.toEntity(userRequestDTO);
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        
        User savedUser = userRepository.save(user);
        return UserMapper.toResponseDTO(savedUser);
    }

    public Optional<UserResponseDTO> authenticateUserAndGetUser(String usernameOrEmail, String password) {
        Optional<User> userOpt = findUserByUsernameOrEmail(usernameOrEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(UserMapper.toResponseDTO(user));
            }
        }
        return Optional.empty();
    }

    private Optional<User> findUserByUsernameOrEmail(String usernameOrEmail) {
        // Try to find user by username first
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        
        // If not found by username, try by email
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        
        return userOpt;
    }



    public Optional<UserResponseDTO> findByIdAsDTO(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponseDTO);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public List<UserResponseDTO> findAllAsDTO() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }




    public UserResponseDTO updateUser(Long userId, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // For password changes, require current password verification
        if (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().trim().isEmpty()) {
            if (updateDTO.getCurrentPassword() == null || updateDTO.getCurrentPassword().trim().isEmpty()) {
                throw new ValidationException("Current password is required for password changes");
            }
            
            // Verify current password
            if (!passwordEncoder.matches(updateDTO.getCurrentPassword(), user.getPassword())) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }
        }
        
        boolean hasChanges = false;
        
        // Update username if provided
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().trim().isEmpty()) {
            // Check if username already exists (excluding current user)
            if (!updateDTO.getUsername().equals(user.getUsername()) && 
                userRepository.findByUsername(updateDTO.getUsername()).isPresent()) {
                throw new DuplicateResourceException("Username", updateDTO.getUsername());
            }
            user.setUsername(updateDTO.getUsername());
            hasChanges = true;
        }
        
        // Update email if provided
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty()) {
            // Check if email already exists (excluding current user)
            if (!updateDTO.getEmail().equals(user.getEmail()) && 
                userRepository.findByEmail(updateDTO.getEmail()).isPresent()) {
                throw new DuplicateResourceException("Email", updateDTO.getEmail());
            }
            user.setEmail(updateDTO.getEmail());
            hasChanges = true;
        }
        
        // Update password if provided
        if (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDTO.getNewPassword()));
            hasChanges = true;
        }
        
        if (!hasChanges) {
            throw new ValidationException("At least one field must be provided for update");
        }
        
        // For password changes, ensure new password meets requirements
        if (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().trim().isEmpty()) {
            if (updateDTO.getNewPassword().length() < 6) {
                throw new ValidationException("New password must be at least 6 characters long");
            }
        }
        
        User updatedUser = userRepository.save(user);
        return UserMapper.toResponseDTO(updatedUser);
    }

}