package com.recipehub.backendrecipehub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.recipehub.backendrecipehub.converter.IngredientListConverter;
import com.recipehub.backendrecipehub.model.Ingredient;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"author", "originalRecipe"})
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_recipe_id") // allow null
    @JsonIgnore
    private Recipe originalRecipe;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Convert(converter = com.recipehub.backendrecipehub.converter.IngredientListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Ingredient> ingredients;

    @Convert(converter = com.recipehub.backendrecipehub.converter.InstructionListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> instructions;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private boolean isPublic;
    private boolean cooked;
    private boolean favourite;

    private int likeCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}