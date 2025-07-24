package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    
    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
    }

    public Recipe createRecipe(Recipe recipe) {
        // Validate that author exists
        if (recipe.getAuthor() == null || recipe.getAuthor().getId() == null) {
            throw new RuntimeException("Author must not be null");
        }
        
        Long userId = recipe.getAuthor().getId();
        User managedUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        recipe.setAuthor(managedUser);
        return recipeRepository.save(recipe);
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public List<Recipe> getPublicRecipes() {
        return recipeRepository.findByIsPublicTrue();
    }

    public List<Recipe> searchByTitle(String title) {
        return recipeRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Recipe> searchByIngredient(String ingredient) {
        return recipeRepository.findByIngredientName(ingredient);
    }

    // Get all recipes by a specific user
    public List<Recipe> getRecipesByUserId(Long userId) {
        return recipeRepository.findByAuthorId(userId);
    }

    // Get all public recipes by a specific user
    public List<Recipe> getPublicRecipesByUserId(Long userId) {
        return recipeRepository.findByAuthorIdAndIsPublicTrue(userId);
    }

    // Get all cooked recipes by a specific user
    public List<Recipe> getCookedRecipesByUserId(Long userId) {
        return recipeRepository.findByAuthorIdAndCookedTrue(userId);
    }

    // Get all favorite recipes by a specific user
    public List<Recipe> getFavoriteRecipesByUserId(Long userId) {
        return recipeRepository.findByAuthorIdAndFavouriteTrue(userId);
    }
}
