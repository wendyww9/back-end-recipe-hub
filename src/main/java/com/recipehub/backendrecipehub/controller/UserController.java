package com.recipehub.backendrecipehub.controller;

import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.service.UserService;
import com.recipehub.backendrecipehub.service.RecipeService;
import com.recipehub.backendrecipehub.dto.RecipeResponseDTO;
import com.recipehub.backendrecipehub.mapper.RecipeMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    private final RecipeService recipeService;

    @Autowired
    public UserController(UserService userService, RecipeService recipeService) {
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            if (user != null) {
                user.setPassword(null); // Don't return password
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    user.setPassword(null); // Don't return password
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<User> getAllUsers() {
        List<User> users = userService.findAll();
        users.forEach(user -> user.setPassword(null)); // Don't return passwords
        return users;
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long userId, @RequestBody Map<String, String> updatePasswordMap) {
        try {
            User updatedUser = userService.updatePassword(userId, updatePasswordMap);
            updatedUser.setPassword(null); // Don't return password
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{userId}/email")
    public ResponseEntity<?> updateEmail(@PathVariable Long userId, @RequestBody Map<String, String> updateEmailMap) {
        try {
            User updatedUser = userService.updateEmail(userId, updateEmailMap);
            updatedUser.setPassword(null); // Don't return password
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{userId}/recipes")
    public ResponseEntity<List<RecipeResponseDTO>> getUserRecipes(@PathVariable Long userId) {
        try {
            List<RecipeResponseDTO> recipeDTOs = recipeService.getRecipesByUserIdAsDTOs(userId);
            return ResponseEntity.ok(recipeDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}