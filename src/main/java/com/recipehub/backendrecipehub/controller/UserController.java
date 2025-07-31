package com.recipehub.backendrecipehub.controller;

import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.service.UserService;
import com.recipehub.backendrecipehub.service.RecipeService;
import com.recipehub.backendrecipehub.dto.RecipeResponseDTO;
import com.recipehub.backendrecipehub.dto.UserResponseDTO;
import com.recipehub.backendrecipehub.dto.UserUpdateDTO;
import com.recipehub.backendrecipehub.mapper.RecipeMapper;
import com.recipehub.backendrecipehub.model.RecipeBook;
import com.recipehub.backendrecipehub.service.RecipeBookService;
import com.recipehub.backendrecipehub.dto.RecipeBookDTO;
import com.recipehub.backendrecipehub.mapper.RecipeBookMapper;
import com.recipehub.backendrecipehub.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    private final RecipeService recipeService;
    private final RecipeBookService recipeBookService;

    @Autowired
    public UserController(UserService userService, RecipeService recipeService, RecipeBookService recipeBookService) {
        this.userService = userService;
        this.recipeService = recipeService;
        this.recipeBookService = recipeBookService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            if (user != null) {
                return ResponseEntity.ok(UserMapper.toResponseDTO(user));
            }
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@Positive @PathVariable Long id) {
        return userService.findByIdAsDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.findAllAsDTO();
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@Positive @PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{userId}/password")
    public ResponseEntity<UserResponseDTO> updatePassword(
            @Positive @PathVariable Long userId, 
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        UserResponseDTO updatedUser = userService.updatePassword(userId, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{userId}/email")
    public ResponseEntity<UserResponseDTO> updateEmail(
            @Positive @PathVariable Long userId, 
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        UserResponseDTO updatedUser = userService.updateEmail(userId, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Positive @PathVariable Long userId, 
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        UserResponseDTO updatedUser = userService.updateUser(userId, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }
    
    @GetMapping("/{userId}/recipes")
    public ResponseEntity<List<RecipeResponseDTO>> getUserRecipes(@Positive @PathVariable Long userId) {
        List<RecipeResponseDTO> recipeDTOs = recipeService.getRecipesByUserId(userId);
        return ResponseEntity.ok(recipeDTOs);
    }

    @GetMapping("/{userId}/recipes/cooked")
    public ResponseEntity<List<RecipeResponseDTO>> getCookedRecipes(@Positive @PathVariable Long userId) {
        List<RecipeResponseDTO> recipeDTOs = recipeService.getUserCookedRecipes(userId);
        return ResponseEntity.ok(recipeDTOs);
    }

    @GetMapping("/{userId}/recipes/favourite")
    public ResponseEntity<List<RecipeResponseDTO>> getFavouriteRecipes(@Positive @PathVariable Long userId) {
        List<RecipeResponseDTO> recipeDTOs = recipeService.getUserFavouriteRecipes(userId);
        return ResponseEntity.ok(recipeDTOs);
    }

    @GetMapping("/{userId}/recipe-books")
    public ResponseEntity<List<RecipeBookDTO>> getUserRecipeBooks(@Positive @PathVariable Long userId) {
        List<RecipeBookDTO> recipeBooks = recipeBookService.getUsersAllRecipeBook(userId);
        return ResponseEntity.ok(recipeBooks);
    }
}