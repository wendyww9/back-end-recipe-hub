package com.recipehub.backendrecipehub.dto;

import lombok.Data;

@Data
public class TagDTO {
    private Long id;
    private String name;
    private int recipeCount;

    public TagDTO(Long id, String name, int recipeCount) {
        this.id = id;
        this.name = name;
        this.recipeCount = recipeCount;
    }

}
