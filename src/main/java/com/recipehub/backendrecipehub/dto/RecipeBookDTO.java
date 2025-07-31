package com.recipehub.backendrecipehub.dto;

import java.util.List;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Data
public class RecipeBookDTO {
    private Long id;
    
    @NotBlank(message = "Recipe book name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Boolean isPublic;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private List<Long> recipeIds;
}
