package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.IngredientDTO;
import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.RecipeResponseDTO;
import com.recipehub.backendrecipehub.mapper.RecipeMapper;
import com.recipehub.backendrecipehub.model.Ingredient;
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
    private final UserRepository userRepository;
    public  RecipeService(RecipeRepository recipeRepository, UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
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

    public RecipeResponseDTO forkRecipe(Long recipeToForkId, RecipeRequestDTO modifications, Long userId) {
        // Find the recipe being forked
        Recipe recipeToFork = recipeRepository.findById(recipeToForkId)
                .orElseThrow(() -> new RuntimeException("Recipe to fork not found"));

        // Find the user who is forking
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Determine the original recipe
        Recipe originalRecipe = recipeToFork.getOriginalRecipe();
        if (originalRecipe == null) {
            // If the recipe being forked has no original, it is the original
            originalRecipe = recipeToFork;
        }

        // Create a DTO from the recipe being forked
        RecipeRequestDTO forkDTO = new RecipeRequestDTO();
        forkDTO.setTitle(recipeToFork.getTitle());
        forkDTO.setDescription(recipeToFork.getDescription());
        forkDTO.setIngredients(recipeToFork.getIngredients().stream()
                .map(ingredient -> {
                    IngredientDTO ingredientDTO = new IngredientDTO();
                    ingredientDTO.setName(ingredient.getName());
                    ingredientDTO.setUnit(ingredient.getUnit());
                    ingredientDTO.setQuantity(ingredient.getQuantity());
                    return ingredientDTO;
                })
                .collect(Collectors.toList()));
        forkDTO.setInstructions(recipeToFork.getInstructions());
        forkDTO.setIsPublic(recipeToFork.isPublic());
        forkDTO.setCooked(recipeToFork.isCooked());
        forkDTO.setFavourite(recipeToFork.isFavourite());
        forkDTO.setLikeCount(0); // Reset like count for new fork

        // Apply modifications if provided
        if (modifications != null) {
            if (modifications.getTitle() != null) {
                forkDTO.setTitle(modifications.getTitle());
            }
            if (modifications.getDescription() != null) {
                forkDTO.setDescription(modifications.getDescription());
            }
            if (modifications.getIngredients() != null) {
                forkDTO.setIngredients(modifications.getIngredients());
            }
            if (modifications.getInstructions() != null) {
                forkDTO.setInstructions(modifications.getInstructions());
            }
            if (modifications.getIsPublic() != null) {
                forkDTO.setIsPublic(modifications.getIsPublic());
            }
            if (modifications.getCooked() != null) {
                forkDTO.setCooked(modifications.getCooked());
            }
            if (modifications.getFavourite() != null) {
                forkDTO.setFavourite(modifications.getFavourite());
            }
        }

        // Use the existing createRecipe method
        Recipe forkedRecipe = createRecipe(forkDTO, user, originalRecipe);
        return RecipeMapper.toDTO(forkedRecipe);
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