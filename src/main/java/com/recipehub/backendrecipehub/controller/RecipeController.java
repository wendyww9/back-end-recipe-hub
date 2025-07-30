package com.recipehub.backendrecipehub.controller;

import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.RecipeResponseDTO;
import com.recipehub.backendrecipehub.exception.RecipeNotFoundException;
import com.recipehub.backendrecipehub.exception.UserNotFoundException;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.service.RecipeService;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.mapper.RecipeMapper;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    private final RecipeService recipeService;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeController(RecipeService recipeService, UserRepository userRepository, RecipeRepository recipeRepository) {
        this.recipeService = recipeService;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
    }

    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(@Valid @RequestBody RecipeRequestDTO requestDTO) {
        User author = userRepository.findById(requestDTO.getAuthorId())
                .orElseThrow(() -> new UserNotFoundException(requestDTO.getAuthorId()));

        Recipe originalRecipe = null;
        if (requestDTO.getOriginalRecipeId() != null) {
            originalRecipe = recipeRepository.findById(requestDTO.getOriginalRecipeId())
                    .orElseThrow(() -> new RecipeNotFoundException(requestDTO.getOriginalRecipeId()));
        }

        RecipeResponseDTO saved = recipeService.createRecipe(requestDTO, author, originalRecipe);
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
    public List<RecipeResponseDTO> searchByTitle(@RequestParam String title) {
        return recipeService.searchByTitle(title);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> getById(@PathVariable Long id) {
        RecipeResponseDTO recipe = recipeService.getRecipeById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));
        return ResponseEntity.ok(recipe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> updateRecipe(@PathVariable Long id, @Valid @RequestBody RecipeRequestDTO requestDTO) {
        // TODO: Get actual user ID from authentication context
        Long userId = requestDTO.getAuthorId();
        RecipeResponseDTO updatedRecipe = recipeService.updateRecipeWithValidation(id, requestDTO, userId);
        return ResponseEntity.ok(updatedRecipe);
    }

    @PutMapping("/{id}/likecount")
    public ResponseEntity<RecipeResponseDTO> updateLikeCount(
        @PathVariable Long id,
        @RequestParam int likeCount) {
            return ResponseEntity.ok(recipeService.updateLikeCount(id, likeCount));
    }

    @PostMapping("/{id}/fork")
    public ResponseEntity<RecipeResponseDTO> forkRecipe(
        @PathVariable Long id,
        @RequestBody(required = false) RecipeRequestDTO modifications) {
        // TODO: Get actual user ID from authentication context
        // For now, we'll use a default user ID of 1
        Long userId = 1L;
        RecipeResponseDTO forkedRecipe = recipeService.forkRecipe(id, modifications, userId);
        return ResponseEntity.ok(forkedRecipe);
    }

}
