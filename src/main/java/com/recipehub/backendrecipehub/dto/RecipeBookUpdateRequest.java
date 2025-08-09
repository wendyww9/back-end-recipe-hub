package com.recipehub.backendrecipehub.dto;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RecipeBookUpdateRequest {
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Boolean isPublic;

    // Optional: when provided, replaces the entire list
    private List<Long> recipeIds;
}


