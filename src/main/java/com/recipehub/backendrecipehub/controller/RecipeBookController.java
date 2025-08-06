package com.recipehub.backendrecipehub.controller;

import com.recipehub.backendrecipehub.dto.RecipeBookDTO;
import com.recipehub.backendrecipehub.service.RecipeBookService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipebooks")
public class RecipeBookController {

    private final RecipeBookService recipeBookService;

    public RecipeBookController(RecipeBookService recipeBookService) {
        this.recipeBookService = recipeBookService;
    }

    @PostMapping
    public ResponseEntity<RecipeBookDTO> createRecipeBook(@Valid @RequestBody RecipeBookDTO recipeBookDTO) {
        RecipeBookDTO createdRecipeBook = recipeBookService.createRecipeBook(recipeBookDTO);
        return ResponseEntity.ok(createdRecipeBook);
    }

    @GetMapping
    public ResponseEntity<List<RecipeBookDTO>> getAllRecipeBooks() {
        List<RecipeBookDTO> recipeBooks = recipeBookService.getAllRecipeBooks();
        return ResponseEntity.ok(recipeBooks);
    }

    @GetMapping("/public")
    public ResponseEntity<List<RecipeBookDTO>> getAllPublicRecipeBooks() {
        List<RecipeBookDTO> recipeBooks = recipeBookService.getAllPublicRecipeBooks();
        return ResponseEntity.ok(recipeBooks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeBookDTO> getRecipeBookById(@Positive @PathVariable Long id) {
        RecipeBookDTO recipeBook = recipeBookService.getRecipeBookById(id);
        return ResponseEntity.ok(recipeBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeBookDTO> updateRecipeBook(
            @Positive @PathVariable Long id, 
            @Valid @RequestBody RecipeBookDTO recipeBookDTO) {
        RecipeBookDTO updatedRecipeBook = recipeBookService.updateRecipeBook(id, recipeBookDTO);
        return ResponseEntity.ok(updatedRecipeBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipeBook(@Positive @PathVariable Long id) {
        recipeBookService.deleteRecipeBook(id);
        return ResponseEntity.noContent().build();
    }   
}
