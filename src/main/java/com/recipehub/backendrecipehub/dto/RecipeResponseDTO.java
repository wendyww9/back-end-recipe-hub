package com.recipehub.backendrecipehub.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecipeResponseDTO {
    private Long id;
    private String title;
    private String description;
    private List<IngredientDTO> ingredients;
    private List<String> instructions;
    private String imageUrl;
    private boolean isPublic;
    private boolean cooked;
    private boolean favourite;
    private int likeCount;
    private Long authorId;
    private String authorUsername;
    private Long originalRecipeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Explicit getter and setter for isPublic to avoid Lombok naming conflicts
    public boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}