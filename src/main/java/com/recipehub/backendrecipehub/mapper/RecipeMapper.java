package com.recipehub.backendrecipehub.mapper;

import com.recipehub.backendrecipehub.dto.*;
import com.recipehub.backendrecipehub.model.*;
import java.util.stream.Collectors;

public class RecipeMapper {
    public static Recipe toEntity(RecipeRequestDTO dto, User user, Recipe originalRecipe) {
        return Recipe.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .ingredients(dto.getIngredients().stream().map(RecipeMapper::mapIngredient).collect(Collectors.toList()))
                .instructions(dto.getInstructions())
                .imageUrl(dto.getImageUrl())
                .isPublic(dto.getIsPublic())
                .cooked(dto.getCooked())
                .favourite(dto.getFavourite())
                .author(user)
                .originalRecipe(originalRecipe)
                .build();
    }

    public static RecipeResponseDTO toDTO(Recipe recipe) {
        RecipeResponseDTO dto = new RecipeResponseDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setIngredients(recipe.getIngredients().stream().map(RecipeMapper::mapIngredientDto).collect(Collectors.toList()));
        dto.setInstructions(recipe.getInstructions());
        dto.setImageUrl(recipe.getImageUrl());
        dto.setIsPublic(recipe.isPublic());
        dto.setCooked(recipe.isCooked());
        dto.setFavourite(recipe.isFavourite());
        dto.setLikeCount(recipe.getLikeCount());
        dto.setAuthorId(recipe.getAuthor().getId());
        dto.setAuthorUsername(recipe.getAuthor().getUsername());
        dto.setOriginalRecipeId(recipe.getOriginalRecipe() != null ? recipe.getOriginalRecipe().getId() : null);
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setUpdatedAt(recipe.getUpdatedAt());
        
        // Map tags
        if (recipe.getTags() != null) {
            dto.setTags(recipe.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private static Ingredient mapIngredient(IngredientDTO dto) {
        return Ingredient.builder()
                .name(dto.getName())
                .unit(dto.getUnit())
                .quantity(dto.getQuantity())
                .build();
    }

    private static IngredientDTO mapIngredientDto(Ingredient ingredient) {
        IngredientDTO dto = new IngredientDTO();
        dto.setName(ingredient.getName());
        dto.setUnit(ingredient.getUnit());
        dto.setQuantity(ingredient.getQuantity());
        return dto;
    }

    public static void updateEntity(RecipeRequestDTO dto, Recipe recipe) {
        // Only update fields that are provided (not null)
        if (dto.getTitle() != null) {
            recipe.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            recipe.setDescription(dto.getDescription());
        }
        if (dto.getIngredients() != null) {
            recipe.setIngredients(dto.getIngredients().stream().map(RecipeMapper::mapIngredient).collect(Collectors.toList()));
        }
        if (dto.getInstructions() != null) {
            recipe.setInstructions(dto.getInstructions());
        }
        if (dto.getImageUrl() != null) {
            // Handle empty string as null (to remove image)
            if (dto.getImageUrl().trim().isEmpty()) {
                recipe.setImageUrl(null);
            } else {
                recipe.setImageUrl(dto.getImageUrl());
            }
        }
        if (dto.getIsPublic() != null) {
            recipe.setPublic(dto.getIsPublic());
        }
        if (dto.getCooked() != null) {
            recipe.setCooked(dto.getCooked());
        }
        if (dto.getFavourite() != null) {
            recipe.setFavourite(dto.getFavourite());
        }
        if (dto.getLikeCount() != null) {
            recipe.setLikeCount(dto.getLikeCount());
        }
        // Note: Tags are handled separately in the service layer
    }
}