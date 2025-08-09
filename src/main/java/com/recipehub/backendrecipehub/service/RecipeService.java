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
import com.recipehub.backendrecipehub.model.Tag;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.RecipeBookRepository;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.TagRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.specification.RecipeSpecification;
import com.recipehub.backendrecipehub.service.S3Service;
import com.recipehub.backendrecipehub.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final RecipeBookRepository recipeBookRepository;
    private final S3Service s3Service;
    private final TagService tagService;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository, 
                       TagRepository tagRepository, RecipeBookRepository recipeBookRepository, S3Service s3Service, TagService tagService) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.recipeBookRepository = recipeBookRepository;
        this.s3Service = s3Service;
        this.tagService = tagService;
    }

    public RecipeResponseDTO createRecipe(RecipeRequestDTO dto, User user, Recipe originalRecipe) {
        Recipe entity = RecipeMapper.toEntity(dto, user, originalRecipe);
        // Set initial updatedAt timestamp
        entity.setUpdatedAt(LocalDateTime.now());
        
        // Handle tags if provided
        if (dto.getTagNames() != null && !dto.getTagNames().isEmpty()) {
            List<Tag> tags = tagService.resolveTagsByName(dto.getTagNames());
            entity.setTags(tags);
        }
        
        Recipe savedRecipe = recipeRepository.save(entity);
        return RecipeMapper.toDTO(savedRecipe);
    }

    @Transactional
    public RecipeResponseDTO createRecipeWithValidation(RecipeRequestDTO requestDTO) {
        // Validate user exists
        User author = userRepository.findById(requestDTO.getAuthorId())
                .orElseThrow(() -> new RecipeNotFoundException("User not found with ID: " + requestDTO.getAuthorId()));

        // Find original recipe if specified
        Recipe originalRecipe = null;
        if (requestDTO.getOriginalRecipeId() != null) {
            originalRecipe = recipeRepository.findById(requestDTO.getOriginalRecipeId())
                    .orElse(null); // Don't throw exception if not found
        }

        Recipe recipe = RecipeMapper.toEntity(requestDTO, author, originalRecipe);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());

        // Handle tags
        if (requestDTO.getTagNames() != null && !requestDTO.getTagNames().isEmpty()) {
            List<Tag> tags = tagService.resolveTagsByName(requestDTO.getTagNames());
            recipe.setTags(tags);
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return RecipeMapper.toDTO(savedRecipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponseDTO> getAllRecipes() {
        List<Recipe> recipes = recipeRepository.findAll();
        return recipes.stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecipeResponseDTO> getAllPublicRecipes() {
        List<Recipe> recipes = recipeRepository.findByIsPublicTrue();
        return recipes.stream()
                .map(RecipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RecipeResponseDTO> getRecipeById(Long id) {
        return recipeRepository.findById(id)
                .map(RecipeMapper::toDTO);
    }

    public RecipeResponseDTO updateRecipe(Long id, RecipeRequestDTO dto) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));
        
        // // Check if the user is the owner of the recipe
        // if (!recipe.getAuthor().getId().equals(userId)) {
        //     throw new UnauthorizedException("Only the recipe owner can update this recipe");
        // }
        
        RecipeMapper.updateEntity(dto, recipe);
        
        // Update tags if provided
        if (dto.getTagNames() != null) {
            List<Tag> tags = tagService.resolveTagsByName(dto.getTagNames());
            recipe.setTags(tags);
        }
        
        // Update the updatedAt timestamp only when recipe is actually modified
        recipe.setUpdatedAt(LocalDateTime.now());
        
        Recipe savedRecipe = recipeRepository.save(recipe);
        return RecipeMapper.toDTO(savedRecipe);
    }

    @Transactional
    public RecipeResponseDTO updateRecipeWithValidation(Long id, RecipeRequestDTO requestDTO) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        // // Check if user is authorized to update this recipe
        // if (!existingRecipe.getAuthor().getId().equals(userId)) {
        //     throw new UnauthorizedException("User not authorized to update this recipe");
        // }

        // Update basic fields using the mapper (only non-null fields)
        RecipeMapper.updateEntity(requestDTO, existingRecipe);
        existingRecipe.setUpdatedAt(LocalDateTime.now());

        // Handle tags with granular control
        if (requestDTO.getTagNames() != null) {
            // Legacy behavior: replace all tags
            List<Tag> tags = tagService.resolveTagsByName(requestDTO.getTagNames());
            existingRecipe.setTags(tags);
        } else {
            // New granular tag management
            List<Tag> currentTags = new ArrayList<>(existingRecipe.getTags());
            
            // Add new tags
            if (requestDTO.getTagsToAdd() != null && !requestDTO.getTagsToAdd().isEmpty()) {
                List<Tag> tagsToAdd = tagService.resolveTagsByName(requestDTO.getTagsToAdd());
                for (Tag tag : tagsToAdd) {
                    // Check if tag already exists by name (case-insensitive)
                    boolean tagExists = currentTags.stream()
                        .anyMatch(t -> t.getName().equalsIgnoreCase(tag.getName()));
                    if (!tagExists) {
                        currentTags.add(tag);
                    }
                }
            }
            
            // Remove tags
            if (requestDTO.getTagsToDelete() != null && !requestDTO.getTagsToDelete().isEmpty()) {
                List<Tag> tagsToDelete = tagService.resolveTagsByName(requestDTO.getTagsToDelete());
                currentTags.removeIf(tag -> tagsToDelete.stream().anyMatch(t -> t.getId().equals(tag.getId())));
            }
            
            existingRecipe.setTags(currentTags);
        }

        Recipe updatedRecipe = recipeRepository.save(existingRecipe);
        return RecipeMapper.toDTO(updatedRecipe);
    }

    @Transactional
    public RecipeResponseDTO updateLikeCount(Long id, int likeCount) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));
        
        recipe.setLikeCount(likeCount);
        recipe.setUpdatedAt(LocalDateTime.now());
        
        Recipe updatedRecipe = recipeRepository.save(recipe);
        return RecipeMapper.toDTO(updatedRecipe);
    }

    @Transactional
    public RecipeResponseDTO forkRecipe(Long originalId, RecipeRequestDTO modifications, Long userId) {
        Recipe originalRecipe = recipeRepository.findById(originalId)
                .orElseThrow(() -> new RecipeNotFoundException(originalId));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RecipeNotFoundException("User not found with ID: " + userId));

        Recipe forkedRecipe = new Recipe();
        forkedRecipe.setTitle(modifications != null && modifications.getTitle() != null ? 
                modifications.getTitle() : originalRecipe.getTitle() + " (Forked)");
        forkedRecipe.setDescription(modifications != null && modifications.getDescription() != null ? 
                modifications.getDescription() : originalRecipe.getDescription());
        forkedRecipe.setIngredients(originalRecipe.getIngredients()); // Use original ingredients
        forkedRecipe.setInstructions(modifications != null && modifications.getInstructions() != null ? 
                modifications.getInstructions() : originalRecipe.getInstructions());
        forkedRecipe.setPublic(modifications != null && modifications.getIsPublic() != null ? 
                modifications.getIsPublic() : originalRecipe.isPublic());
        forkedRecipe.setCooked(false);
        forkedRecipe.setFavourite(false);
        forkedRecipe.setLikeCount(0);
        forkedRecipe.setAuthor(author);
        forkedRecipe.setOriginalRecipe(originalRecipe);
        forkedRecipe.setCreatedAt(LocalDateTime.now());
        forkedRecipe.setUpdatedAt(LocalDateTime.now());

        // === Handle tags BEFORE saving ===
        List<Tag> tagsToAssign;

        if (modifications != null && modifications.getTagNames() != null && !modifications.getTagNames().isEmpty()) {
            // Use provided tags (validated)
            tagsToAssign = tagService.resolveTagsByName(modifications.getTagNames());
        } else if (originalRecipe.getTags() != null && !originalRecipe.getTags().isEmpty()) {
            // Deep copy of managed tags
            tagsToAssign = originalRecipe.getTags().stream()
                    .map(tag -> tagRepository.findById(tag.getId())
                            .orElseThrow(() -> new ValidationException("Original tag not found: " + tag.getName())))
                    .collect(Collectors.toList());
        } else {
            tagsToAssign = new ArrayList<>();
        }

        // Always assign a fresh list
        forkedRecipe.setTags(new ArrayList<>(tagsToAssign));

        // === THEN save the recipe ===
        Recipe savedForkedRecipe = recipeRepository.save(forkedRecipe);
        return RecipeMapper.toDTO(savedForkedRecipe);
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

    // Enhanced search method with multiple criteria using JPA Specifications
    @Transactional(readOnly = true)
    public List<RecipeResponseDTO> searchRecipes(
            String title, List<String> tags, String author, Boolean isPublic,
            Boolean cooked, Boolean favourite, String difficulty, String cuisine,
            String mealType, String dietary, String cookingMethod, String occasion,
            String season, String health, String ingredient, String specialFeature) {
        
        // Build specification
        Specification<Recipe> spec = Specification.where(null);
        
        if (title != null && !title.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasTitle(title));
        }
        
        if (isPublic != null) {
            spec = spec.and(RecipeSpecification.isPublic(isPublic));
        }
        
        if (cooked != null) {
            spec = spec.and(RecipeSpecification.isCooked(cooked));
        }
        
        if (favourite != null) {
            spec = spec.and(RecipeSpecification.isFavourite(favourite));
        }
        
        if (author != null && !author.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasAuthor(author));
        }
        
        if (tags != null && !tags.isEmpty()) {
            spec = spec.and(RecipeSpecification.hasAnyTag(tags));
        }
        
        if (cuisine != null && !cuisine.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasCuisine(cuisine));
        }
        
        if (difficulty != null && !difficulty.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasDifficulty(difficulty));
        }
        
        if (mealType != null && !mealType.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasMealType(mealType));
        }
        
        if (dietary != null && !dietary.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasDietary(dietary));
        }
        
        if (cookingMethod != null && !cookingMethod.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasCookingMethod(cookingMethod));
        }
        
        if (occasion != null && !occasion.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasOccasion(occasion));
        }
        
        if (season != null && !season.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasSeason(season));
        }
        
        if (health != null && !health.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasHealth(health));
        }
        
        if (ingredient != null && !ingredient.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasIngredient(ingredient));
        }
        
        if (specialFeature != null && !specialFeature.trim().isEmpty()) {
            spec = spec.and(RecipeSpecification.hasSpecialFeature(specialFeature));
        }
        
        // Execute query
        List<Recipe> recipes = recipeRepository.findAll(spec);
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
            Boolean favourite,
            String tagNamesJson
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

        // Parse tagNames if provided
        List<String> tagNames = null;
        if (tagNamesJson != null && !tagNamesJson.trim().isEmpty()) {
            try {
                tagNames = mapper.readValue(
                        tagNamesJson,
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
            } catch (Exception e) {
                // If parsing fails, ignore tagNames
                tagNames = null;
            }
        }

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
        requestDTO.setTagNames(tagNames);

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
        
        // Get the current recipe to check if it has an existing image
        RecipeResponseDTO currentRecipe = getRecipeById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException(recipeId));
        
        String oldImageUrl = currentRecipe.getImageUrl();
        String oldFileName = null;
        
        // Extract old file name if image exists
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            oldFileName = extractFileNameFromUrl(oldImageUrl);
        }
        
        // Upload new image
        String newFileName = s3Service.uploadImage(file);
        String newImageUrl = s3Service.getImageUrl(newFileName);

        // Update recipe with new image URL
        RecipeRequestDTO updateRequest = new RecipeRequestDTO();
        updateRequest.setImageUrl(newImageUrl);

        RecipeResponseDTO updatedRecipe = updateRecipeWithValidation(recipeId, updateRequest);
        
        // Delete old image from S3 if it exists
        if (oldFileName != null) {
            try {
                s3Service.deleteImage(oldFileName);
            } catch (Exception e) {
                // Log the error but don't fail the upload
                // The new image was successfully uploaded and recipe was updated
                System.err.println("Failed to delete old image: " + oldFileName + " - " + e.getMessage());
            }
        }
        
        return updatedRecipe;
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

        return updateRecipeWithValidation(recipeId, updateRequest);
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

    @Transactional
    public void deleteRecipe(Long recipeId) {
        Recipe r = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeNotFoundException(recipeId));
        recipeRepository.delete(r); // triggers @SQLDelete on Recipe only
    // Recipebook entries remain; queries will hide deleted recipe rows.
}
}