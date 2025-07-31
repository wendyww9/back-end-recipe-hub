package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.UserDTO;
import com.recipehub.backendrecipehub.dto.PasswordUpdateDTO;
import com.recipehub.backendrecipehub.dto.EmailUpdateDTO;
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

    public User registerUser(UserDTO userDTO) {
        // Check if username already exists
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username", userDTO.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email", userDTO.getEmail());
        }
        
        // Convert DTO to User entity
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        return userRepository.save(user);
    }

    public User save(User user) {
        // If this is a new user or password has changed, hash it
        if (user.getId() == null || user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
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

    public boolean login(String usernameOrEmail, String password) {
        Optional<User> userOpt = findUserByUsernameOrEmail(usernameOrEmail);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public Optional<User> loginAndGetUser(String usernameOrEmail, String password) {
        Optional<User> userOpt = findUserByUsernameOrEmail(usernameOrEmail);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return userOpt;
            }
        }
        return Optional.empty();
    }

    public boolean loginWithUsername(String username, String password) {
        return authenticateUser(username, password);
    }

    public boolean loginWithEmail(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public boolean logout() {
        // Since this is a stateless service, logout just needs to return success
        // The actual session/token invalidation would typically be handled by
        // the security configuration or controller layer
        return true;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }


    public User updatePassword(Long userId, PasswordUpdateDTO updatePasswordDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        String currentPassword = updatePasswordDTO.getCurrentPassword();
        String newPassword = updatePasswordDTO.getNewPassword();
        
        if (currentPassword == null || newPassword == null) {
            throw new ValidationException("Current password and new password are required");
        }
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User updateEmail(Long userId, EmailUpdateDTO updateEmailDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        String currentPassword = updateEmailDTO.getCurrentPassword();
        String newEmail = updateEmailDTO.getNewEmail();
        
        if (currentPassword == null || newEmail == null) {
            throw new ValidationException("Current password and new email are required");
        }
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        
        // Check if new email already exists
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new DuplicateResourceException("Email", newEmail);
        }
        
        // Update email
        user.setEmail(newEmail);
        return userRepository.save(user);
    }

}