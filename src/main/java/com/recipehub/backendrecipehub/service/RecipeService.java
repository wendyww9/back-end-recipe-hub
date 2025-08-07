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
import com.recipehub.backendrecipehub.service.S3Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnBean(S3Service.class)
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository, S3Service s3Service) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }

    public RecipeResponseDTO createRecipe(RecipeRequestDTO dto, User user, Recipe originalRecipe) {
        Recipe entity = RecipeMapper.toEntity(dto, user, originalRecipe);
        // Set initial updatedAt timestamp
        entity.setUpdatedAt(LocalDateTime.now());
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
        
        // Update the updatedAt timestamp only when recipe is actually modified
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

    // Image handling methods
    public RecipeResponseDTO createRecipeFromRequest(
            MultipartFile file,
            String title,
            String description,
            String ingredientsJson,
            String instructionsJson,
            Long authorId,
            Boolean isPublic,
            Boolean cooked,
            Boolean favourite
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<IngredientDTO> ingredientList = mapper.readValue(
                ingredientsJson,
                mapper.getTypeFactory().constructCollectionType(List.class, IngredientDTO.class)
        );

        List<String> instructionList = mapper.readValue(
                instructionsJson,
                mapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );

        // Create DTO
        RecipeRequestDTO requestDTO = new RecipeRequestDTO();
        requestDTO.setTitle(title);
        requestDTO.setDescription(description);
        requestDTO.setIngredients(ingredientList);
        requestDTO.setInstructions(instructionList);
        requestDTO.setAuthorId(authorId);
        requestDTO.setIsPublic(isPublic != null ? isPublic : false);
        requestDTO.setCooked(cooked != null ? cooked : false);
        requestDTO.setFavourite(favourite != null ? favourite : false);

        if (file != null && !file.isEmpty()) {
            validateImageFile(file);
            String fileName = s3Service.uploadImage(file);
            String imageUrl = s3Service.getImageUrl(fileName);
            requestDTO.setImageUrl(imageUrl);
        }

        return createRecipeWithValidation(requestDTO);
    }



    public RecipeResponseDTO uploadRecipeImage(Long recipeId, MultipartFile file, Long userId) throws IOException {
        validateImageFile(file);
        
        String fileName = s3Service.uploadImage(file);
        String imageUrl = s3Service.getImageUrl(fileName);

        // Update recipe with new image URL
        RecipeRequestDTO updateRequest = new RecipeRequestDTO();
        updateRequest.setImageUrl(imageUrl);

        return updateRecipeWithValidation(recipeId, updateRequest, userId);
    }

    public RecipeResponseDTO deleteRecipeImage(Long recipeId, Long userId) {
        // Get the current recipe to check if it has an image
        RecipeResponseDTO currentRecipe = getRecipeById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException(recipeId));

        String currentImageUrl = currentRecipe.getImageUrl();
        if (currentImageUrl == null || currentImageUrl.isEmpty()) {
            throw new ValidationException("Recipe does not have an image to delete");
        }

        // Extract fileName from the image URL
        String fileName = extractFileNameFromUrl(currentImageUrl);
        if (fileName == null) {
            throw new ValidationException("Invalid image URL format");
        }

        // Delete image from S3
        s3Service.deleteImage(fileName);

        // Update recipe to remove image URL
        RecipeRequestDTO updateRequest = new RecipeRequestDTO();
        updateRequest.setImageUrl(""); // Set to empty string to remove image

        return updateRecipeWithValidation(recipeId, updateRequest, userId);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ValidationException("File must be an image");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ValidationException("File size must be less than 5MB");
        }
    }



    private String extractFileNameFromUrl(String imageUrl) {
        try {
            // Handle both S3 URLs and regular HTTP URLs
            if (imageUrl.contains("s3.amazonaws.com")) {
                // Extract from S3 URL: https://s3.amazonaws.com/bucket/recipe-images/uuid.jpg
                String[] parts = imageUrl.split("/");
                if (parts.length >= 4) {
                    return parts[parts.length - 2] + "/" + parts[parts.length - 1];
                }
            } else if (imageUrl.contains("/")) {
                // Extract from regular URL: https://example.com/images/uuid.jpg
                String[] parts = imageUrl.split("/");
                if (parts.length >= 2) {
                    return parts[parts.length - 2] + "/" + parts[parts.length - 1];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}