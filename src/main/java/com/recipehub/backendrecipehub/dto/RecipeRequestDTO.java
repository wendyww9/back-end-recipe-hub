package com.recipehub.backendrecipehub.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecipeRequestDTO {
    private String title;
    private String description;
    private List<IngredientDTO> ingredients;
    private List<String> instructions;
    private boolean isPublic;
    private boolean cooked;
    private boolean favourite;
    private Long authorId;
    private Long originalRecipeId;
    
    // Explicit getter and setter for isPublic to avoid Lombok naming conflicts
    public boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}