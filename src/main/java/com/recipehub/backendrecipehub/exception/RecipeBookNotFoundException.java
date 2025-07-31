package com.recipehub.backendrecipehub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecipeBookNotFoundException extends RuntimeException {
    
    public RecipeBookNotFoundException(Long id) {
        super("Recipe book not found with id: " + id);
    }
    
    public RecipeBookNotFoundException(String message) {
        super(message);
    }
} 