package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.IngredientDTO;
import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.RecipeResponseDTO;
import com.recipehub.backendrecipehub.exception.RecipeNotFoundException;
import com.recipehub.backendrecipehub.exception.UnauthorizedException;
import com.recipehub.backendrecipehub.exception.UserNotFoundException;
import com.recipehub.backendrecipehub.exception.ValidationException;
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

    public RecipeResponseDTO createRecipe(RecipeRequestDTO dto, User user, Recipe originalRecipe) {
        Recipe entity = RecipeMapper.toEntity(dto, user, originalRecipe);
        Recipe savedRecipe = recipeRepository.save(entity);
        return RecipeMapper.toDTO(savedRecipe);
    }

    public RecipeResponseDTO createRecipeWithValidation(RecipeRequestDTO dto) {
        // Find the user
        User user = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new UserNotFoundException(dto.getAuthorId()));

        // Find the original recipe if specified
        Recipe originalRecipe = null;
        if (dto.getOriginalRecipeId() != null) {
            originalRecipe = recipeRepository.findById(dto.getOriginalRecipeId())
                    .orElseThrow(() -> new RecipeNotFoundException(dto.getOriginalRecipeId()));
        }

        return createRecipe(dto, user, originalRecipe);
    }

    public List<RecipeResponseDTO> getAllRecipes() {
        List<Recipe> recipes = recipeRepository.findAll();
        return recipes.stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RecipeResponseDTO> getAllPublicRecipes() {
        List<Recipe> recipes = recipeRepository.findByIsPublicTrue();
        return recipes.stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<RecipeResponseDTO> getRecipeById(Long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);
        return recipe.map(RecipeMapper::toDTO);
    }

    public RecipeResponseDTO updateRecipe(Long id, RecipeRequestDTO dto, Long userId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));
        
        // Check if the user is the owner of the recipe
        if (!recipe.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("Only the recipe owner can update this recipe");
        }
        
        RecipeMapper.updateEntity(dto, recipe);
        
        // Update the updatedAt timestamp
        recipe.setUpdatedAt(LocalDateTime.now());
        
        Recipe savedRecipe = recipeRepository.save(recipe);
        return RecipeMapper.toDTO(savedRecipe);
    }

    public RecipeResponseDTO updateRecipeWithValidation(Long id, RecipeRequestDTO dto, Long userId) {
        if (userId == null) {
            throw new ValidationException("User ID is required");
        }
        
        return updateRecipe(id, dto, userId);
    }

    public RecipeResponseDTO updateLikeCount(Long id, Integer likeCount) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));
        recipe.setLikeCount(likeCount);
        
        Recipe savedRecipe = recipeRepository.save(recipe);
        return RecipeMapper.toDTO(savedRecipe);
    }

    public RecipeResponseDTO forkRecipe(Long recipeToForkId, RecipeRequestDTO modifications, Long userId) {
        // Find the recipe being forked
        Recipe recipeToFork = recipeRepository.findById(recipeToForkId)
                .orElseThrow(() -> new RecipeNotFoundException(recipeToForkId));

        // Find the user who is forking
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

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
        return createRecipe(forkDTO, user, originalRecipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponseDTO> getRecipesByUserId(Long userId) {
        List<Recipe> recipes = recipeRepository.findByAuthorId(userId);
        return recipes.stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecipeResponseDTO> searchByTitle(String title) {
        List<Recipe> recipes = recipeRepository.findByTitleContainingIgnoreCase(title);
        return recipes.stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }



    public List<RecipeResponseDTO> getUserCookedRecipes(Long userId) {
        List<Recipe> recipes = recipeRepository.findByAuthorIdAndCookedTrue(userId);
        return recipes.stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RecipeResponseDTO> getUserFavouriteRecipes(Long userId) {
        List<Recipe> recipes = recipeRepository.findByAuthorIdAndFavouriteTrue(userId);
        return recipes.stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    // Add more methods if needed
}