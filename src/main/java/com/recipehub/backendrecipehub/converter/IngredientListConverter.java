package com.recipehub.backendrecipehub.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipehub.backendrecipehub.model.Ingredient;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class IngredientListConverter implements AttributeConverter<List<Ingredient>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Ingredient> ingredients) {
        try {
            return objectMapper.writeValueAsString(ingredients);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting ingredients to JSON", e);
        }
    }

    @Override
    public List<Ingredient> convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading ingredients from JSON", e);
        }
    }
}