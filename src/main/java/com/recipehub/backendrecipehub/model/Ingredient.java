package com.recipehub.backendrecipehub.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Ingredient {
    private String name;
    private String unit;
    private double quantity;
}