package com.recipehub.backendrecipehub.repository;

import com.recipehub.backendrecipehub.model.Recipe;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    
    // Search by recipe name (title)
    List<Recipe> findByTitleContainingIgnoreCase(String title);

    // Custom: search by ingredient name (uses LIKE on JSON string)
    @Query("SELECT r FROM Recipe r WHERE LOWER(r.ingredients) LIKE LOWER(CONCAT('%', :ingredientName, '%'))")
    List<Recipe> findByIngredientName(String ingredientName);

    List<Recipe> findByIsPublicTrue();

    // Method naming convention (cleaner)
    List<Recipe> findByAuthorId(Long authorId);
    List<Recipe> findByAuthorIdAndCookedTrue(Long authorId);
    List<Recipe> findByAuthorIdAndFavouriteTrue(Long authorId);

    // Alternative: @Query for complex cases 
    // @Query("SELECT r FROM Recipe r WHERE r.author.id = :authorId")
    // List<Recipe> findByAuthorId(Long authorId);

    // @Query("SELECT r FROM Recipe r WHERE r.author.id = :authorId and r.cooked = true")
    // List<Recipe> findByAuthorIdAndCookedTrue(Long authorId);

    // @Query("SELECT r FROM Recipe r WHERE r.author.id = :authorId and r.favourite = true")
    // List<Recipe> findByAuthorIdAndFavouriteTrue(Long authorId);

}