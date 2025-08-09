package com.recipehub.backendrecipehub.repository;

import com.recipehub.backendrecipehub.model.RecipeBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface RecipeBookRepository extends JpaRepository<RecipeBook, Long> {
    // Find recipe books by user ID
    List<RecipeBook> findByUserId(Long userId);
    // Find public recipe books
    List<RecipeBook> findByIsPublicTrue();
    Optional<RecipeBook> findByIdAndIsPublicTrue(Long id);
}
