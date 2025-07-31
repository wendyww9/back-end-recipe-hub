package com.recipehub.backendrecipehub.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

@Data
public class IngredientDTO {
    
    @NotBlank(message = "Ingredient name is required")
    @Size(min = 1, max = 100, message = "Ingredient name must be between 1 and 100 characters")
    private String name;
    
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;
    
    @Min(value = 0, message = "Quantity cannot be negative")
    private double quantity;
}