package com.recipehub.backendrecipehub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.recipehub.backendrecipehub.converter.IngredientListConverter;
import com.recipehub.backendrecipehub.converter.InstructionListConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"email", "password", "createdAt"})
    private User author;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "original_recipe_id")
    @JsonIgnore
    private Recipe originalRecipe;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Convert(converter = IngredientListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Ingredient> ingredients;

    @Convert(converter = InstructionListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> instructions;

    private boolean isPublic;
    private boolean cooked;
    private boolean favourite;

    private int likeCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
