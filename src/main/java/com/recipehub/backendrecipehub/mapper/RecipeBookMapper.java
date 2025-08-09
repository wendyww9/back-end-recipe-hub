package com.recipehub.backendrecipehub.mapper;

import com.recipehub.backendrecipehub.dto.RecipeBookDTO;
import com.recipehub.backendrecipehub.dto.RecipeBookCreateRequest;
import com.recipehub.backendrecipehub.dto.RecipeBookUpdateRequest;
import com.recipehub.backendrecipehub.model.RecipeBook;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecipeBookMapper {


    public static RecipeBook toEntity(RecipeBookCreateRequest dto, User user) {
        return RecipeBook.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false)
                .user(user)
                .build();
    }

    public static RecipeBookDTO toDTO(RecipeBook recipeBook) {
        RecipeBookDTO dto = new RecipeBookDTO();
        dto.setId(recipeBook.getId());
        dto.setName(recipeBook.getName());
        dto.setDescription(recipeBook.getDescription());
        dto.setIsPublic(recipeBook.isPublic());
        dto.setUserId(recipeBook.getUser().getId());
        
        // Extract recipe IDs from the recipes set
        List<Long> recipeIds = recipeBook.getRecipes().stream()
                .map(Recipe::getId)
                .collect(Collectors.toList());
        dto.setRecipeIds(recipeIds);
        
        return dto;
    }

    public static List<RecipeBookDTO> toDTOList(List<RecipeBook> recipeBooks) {
        return recipeBooks.stream()
                .map(RecipeBookMapper::toDTO)
                .collect(Collectors.toList());
    }


    public static void updateEntity(RecipeBookUpdateRequest updateRequest, RecipeBook recipeBook) {
        if (updateRequest.getName() != null) {
            recipeBook.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            recipeBook.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getIsPublic() != null) {
            recipeBook.setPublic(updateRequest.getIsPublic());
        }
    }
} 