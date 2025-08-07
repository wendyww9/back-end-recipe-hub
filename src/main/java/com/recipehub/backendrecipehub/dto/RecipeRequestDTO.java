package com.recipehub.backendrecipehub.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@Data
public class RecipeRequestDTO {
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Ingredients list is required")
    @Size(min = 1, message = "At least one ingredient is required")
    private List<IngredientDTO> ingredients;
    
    @NotNull(message = "Instructions list is required")
    @Size(min = 1, message = "At least one instruction is required")
    private List<String> instructions;
    
    @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
    @Pattern(regexp = "^(https?://.*|s3://.*|$)", message = "Image URL must be a valid HTTP/HTTPS URL or S3 path")
    private String imageUrl;
    
    private Boolean isPublic;
    private Boolean cooked;
    private Boolean favourite;
    
    @Min(value = 0, message = "Like count cannot be negative")
    private Integer likeCount;
    
    @NotNull(message = "Author ID is required")
    private Long authorId;
    
    private Long originalRecipeId;
    
    // Explicit getter and setter for isPublic to avoid Lombok naming conflicts
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}