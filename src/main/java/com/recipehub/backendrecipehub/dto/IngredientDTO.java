package com.recipehub.backendrecipehub.dto;

import lombok.Data;

@Data
public class IngredientDTO {
    private String name;
    private String unit;
    private double quantity;
}