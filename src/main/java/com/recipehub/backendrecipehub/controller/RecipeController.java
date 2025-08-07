package com.recipehub.backendrecipehub.controller;

import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.RecipeResponseDTO;
import com.recipehub.backendrecipehub.exception.RecipeNotFoundException;
import com.recipehub.backendrecipehub.service.RecipeService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

        @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("ingredients") String ingredients,
            @RequestParam("instructions") String instructions,
            @RequestParam("authorId") Long authorId,
            @RequestParam(value = "isPublic", required = false) Boolean isPublic,
            @RequestParam(value = "cooked", required = false) Boolean cooked,
            @RequestParam(value = "favourite", required = false) Boolean favourite) {
        
        try {
            RecipeResponseDTO saved = recipeService.createRecipeFromRequest(
                file, title, description, ingredients, instructions, 
                authorId, isPublic, cooked, favourite);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create recipe: " + e.getMessage());
        }
    }

    @GetMapping
    public List<RecipeResponseDTO> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    @GetMapping("/public")
    public List<RecipeResponseDTO> getAllPublicRecipes() {
        return recipeService.getAllPublicRecipes();
    }

    @GetMapping("/search")
    public List<RecipeResponseDTO> searchByTitle(@NotNull @RequestParam String title) {
        return recipeService.searchByTitle(title);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> getById(@Positive @PathVariable Long id) {
        RecipeResponseDTO recipe = recipeService.getRecipeById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));
        return ResponseEntity.ok(recipe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> updateRecipe(
            @Positive @PathVariable Long id, 
            @Valid @RequestBody RecipeRequestDTO requestDTO) {
        // TODO: Get actual user ID from authentication context
        Long userId = requestDTO.getAuthorId();
        RecipeResponseDTO updatedRecipe = recipeService.updateRecipeWithValidation(id, requestDTO, userId);
        return ResponseEntity.ok(updatedRecipe);
    }

    @PutMapping("/{id}/likecount")
    public ResponseEntity<RecipeResponseDTO> updateLikeCount(
        @Positive @PathVariable Long id,
        @Min(0) @RequestParam int likeCount) {
            return ResponseEntity.ok(recipeService.updateLikeCount(id, likeCount));
    }

    @PostMapping("/{id}/fork")
    public ResponseEntity<RecipeResponseDTO> forkRecipe(
        @Positive @PathVariable Long id,
        @RequestBody(required = false) RecipeRequestDTO modifications) {
        // TODO: Get actual user ID from authentication context
        // For now, we'll use a default user ID of 1
        Long userId = 1L;
        RecipeResponseDTO forkedRecipe = recipeService.forkRecipe(id, modifications, userId);
        return ResponseEntity.ok(forkedRecipe);
    }



    @PostMapping("/{id}/image")
    @ConditionalOnBean(S3Client.class)
    public ResponseEntity<Map<String, Object>> uploadRecipeImage(
            @Positive @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // TODO: Get actual user ID from authentication context
            Long userId = 1L; // Placeholder
            RecipeResponseDTO updatedRecipe = recipeService.uploadRecipeImage(id, file, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("recipe", updatedRecipe);
            response.put("message", "Recipe image updated successfully");
            return ResponseEntity.ok(response);
        } catch (RecipeNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update recipe: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping("/{id}/image")
    @ConditionalOnBean(S3Client.class)
    public ResponseEntity<Map<String, Object>> deleteRecipeImage(@Positive @PathVariable Long id) {
        
        try {
            // TODO: Get actual user ID from authentication context
            Long userId = 1L; // Placeholder
            RecipeResponseDTO updatedRecipe = recipeService.deleteRecipeImage(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("recipe", updatedRecipe);
            response.put("message", "Recipe image deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RecipeNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete recipe image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
