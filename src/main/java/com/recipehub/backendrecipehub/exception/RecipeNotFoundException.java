package com.recipehub.backendrecipehub.exception;

public class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException(String message) {
        super(message);
    }
    
    public RecipeNotFoundException(Long recipeId) {
        super("Recipe not found with id: " + recipeId);
    }
} 