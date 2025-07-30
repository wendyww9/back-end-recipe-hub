package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.RecipeResponseDTO;
import com.recipehub.backendrecipehub.mapper.RecipeMapper;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.RecipeRepository;

import com.recipehub.backendrecipehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    public  RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Recipe createRecipe(RecipeRequestDTO dto, User author, Recipe originalRecipe) {
        Recipe entity = RecipeMapper.toEntity(dto, author, originalRecipe);
        return recipeRepository.save(entity);
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public List<Recipe> getAllPublicRecipes() {
        return recipeRepository.findByIsPublicTrue();
    }

    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public Recipe updateRecipe(Long id, RecipeRequestDTO dto, Long userId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        
        // Check if the user is the author of the recipe
        if (!recipe.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only the recipe author can update this recipe");
        }
        
        RecipeMapper.updateEntity(dto, recipe);
        
        // Update the updatedAt timestamp
        recipe.setUpdatedAt(LocalDateTime.now());
        
        return recipeRepository.save(recipe);
    }

    public RecipeResponseDTO updateRecipeWithValidation(Long id, RecipeRequestDTO dto, Long userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        
        Recipe updatedRecipe = updateRecipe(id, dto, userId);
        return RecipeMapper.toDTO(updatedRecipe);
    }

    public RecipeResponseDTO updateLikeCount(Long id, Integer likeCount) {
        
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        recipe.setLikeCount(likeCount);
        
        Recipe savedRecipe = recipeRepository.save(recipe);
        return RecipeMapper.toDTO(savedRecipe);
    }

    @Transactional(readOnly = true)
    public List<Recipe> getRecipesByUserId(Long userId) {
        return recipeRepository.findByAuthorId(userId);
    }

    // Method to get recipes by user ID and convert to DTOs with error handling
    @Transactional(readOnly = true)
    public List<RecipeResponseDTO> getRecipesByUserIdAsDTOs(Long userId) {
        try {
            List<Recipe> recipes = getRecipesByUserId(userId);
            System.out.println("Found " + recipes.size() + " recipes for user " + userId);
            
            return recipes.stream()
                    .map(recipe -> {
                        try {
                            return RecipeMapper.toDTO(recipe);
                        } catch (Exception e) {
                            System.err.println("Error mapping recipe: " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getRecipesByUserIdAsDTOs: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve recipes for user: " + userId, e);
        }
    }


    // Add more methods if needed
}