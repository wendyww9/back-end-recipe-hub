package com.recipehub.backendrecipehub.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecipeRequestDTO {
    private String title;
    private String description;
    private List<IngredientDTO> ingredients;
    private List<String> instructions;
    private Boolean isPublic;
    private Boolean cooked;
    private Boolean favourite;
    private Integer likeCount;
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