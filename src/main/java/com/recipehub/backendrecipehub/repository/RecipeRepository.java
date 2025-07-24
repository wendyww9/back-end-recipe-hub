package com.recipehub.backendrecipehub.repository;

import com.recipehub.backendrecipehub.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByIsPublicTrue();

    List<Recipe> findByAuthorId(Long authorId);

    List<Recipe> findByAuthorIdAndFavouriteTrue(Long authorId);

    List<Recipe> findByAuthorIdAndCookedTrue(Long authorId);

    List<Recipe> findByAuthorIdAndIsPublicTrue(Long authorId);

    List<Recipe> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT r FROM Recipe r where LOWER(r.ingredients) LIKE LOWER(CONCAT('%', :ingredientName, '%'))")
    List<Recipe> findByIngredientName(String ingredientName);
}
