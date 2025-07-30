package com.recipehub.backendrecipehub.controller;

import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.RecipeResponseDTO;
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
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Recipe originalRecipe = null;
        if (requestDTO.getOriginalRecipeId() != null) {
            originalRecipe = recipeRepository.findById(requestDTO.getOriginalRecipeId())
                    .orElseThrow(() -> new RuntimeException("Original recipe not found"));
        }

        Recipe saved = recipeService.createRecipe(requestDTO, author, originalRecipe);
        return ResponseEntity.ok(RecipeMapper.toDTO(saved));
    }

    @GetMapping
    public List<RecipeResponseDTO> getAllRecipes() {
        return recipeService.getAllPublicRecipes()
                .stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/public")
    public List<RecipeResponseDTO> getAllPublicRecipes() {
        return recipeService.getAllPublicRecipes()
                .stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> getById(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        return ResponseEntity.ok(RecipeMapper.toDTO(recipe));
    }
}
