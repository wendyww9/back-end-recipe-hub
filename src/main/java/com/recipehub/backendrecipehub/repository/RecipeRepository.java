package com.recipehub.backendrecipehub.repository;

import com.recipehub.backendrecipehub.model.Recipe;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByIsPublicTrue();

    List<Recipe> findByAuthorId(Long authorId);

    List<Recipe> findByAuthorIdAndCookedTrue(Long authorId);

    List<Recipe> findByAuthorIdAndFavouriteTrue(Long authorId);

    // Search by recipe name (title)
    List<Recipe> findByTitleContainingIgnoreCase(String title);

    // Custom: search by ingredient name (uses LIKE on JSON string)
    @Query("SELECT r FROM Recipe r WHERE LOWER(r.ingredients) LIKE LOWER(CONCAT('%', :ingredientName, '%'))")
    List<Recipe> findByIngredientName(String ingredientName);
}