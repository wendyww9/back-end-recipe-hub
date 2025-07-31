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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<RecipeResponseDTO> createRecipe(@Valid @RequestBody RecipeRequestDTO requestDTO) {
        RecipeResponseDTO saved = recipeService.createRecipeWithValidation(requestDTO);
        return ResponseEntity.ok(saved);
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
}
