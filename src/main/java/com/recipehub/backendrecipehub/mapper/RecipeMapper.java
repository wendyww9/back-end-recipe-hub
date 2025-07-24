package com.recipehub.backendrecipehub.mapper;

import com.recipehub.backendrecipehub.dto.*;
import com.recipehub.backendrecipehub.model.*;
import java.util.stream.Collectors;

public class RecipeMapper {
    public static Recipe toEntity(RecipeRequestDTO dto, User author, Recipe originalRecipe) {
        return Recipe.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .ingredients(dto.getIngredients().stream().map(RecipeMapper::mapIngredient).collect(Collectors.toList()))
                .instructions(dto.getInstructions())
                .isPublic(dto.getIsPublic())
                .cooked(dto.isCooked())
                .favourite(dto.isFavourite())
                .author(author)
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
        dto.setIsPublic(recipe.isPublic());
        dto.setCooked(recipe.isCooked());
        dto.setFavourite(recipe.isFavourite());
        dto.setLikeCount(recipe.getLikeCount());
        dto.setAuthorId(recipe.getAuthor().getId());
        dto.setAuthorUsername(recipe.getAuthor().getUsername());
        dto.setOriginalRecipeId(recipe.getOriginalRecipe() != null ? recipe.getOriginalRecipe().getId() : null);
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setUpdatedAt(recipe.getUpdatedAt());
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
}