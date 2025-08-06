package com.recipehub.backendrecipehub.model;

import com.recipehub.backendrecipehub.model.Recipe;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private List<Recipe> recipes = new ArrayList<>();
}
