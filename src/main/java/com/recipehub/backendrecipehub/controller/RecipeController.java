package com.recipehub.backendrecipehub.controller;

import com.recipehub.backendrecipehub.model.Recipe;
//import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {
//
//    @Autowired
    private final RecipeService recipeService;
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

//    @PostMapping(
//            value = "",
//            consumes = {"application/json", "application/json;charset=UTF-8"}
//    )
//    public Recipe createRecipe(@RequestBody Recipe recipe) {
//        System.out.println("📥 Incoming recipe: " + recipe.getTitle());
//        System.out.println("👤 Incoming author: " + recipe.getAuthor());
//
//        if (recipe.getAuthor() == null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing 'author' in JSON");
//        }
//        System.out.println("📥 Creating recipe: " + recipe.getTitle());
//        System.out.println("👤 Author ID: " + (recipe.getAuthor() != null ? recipe.getAuthor().getId() : "null"));
//        return recipeService.createRecipe(recipe);
//    }
//    @PostMapping(
////            consumes = {"application/json", "application/json;charset=UTF-8"},
//            produces = "application/json")
//    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
//        System.out.println("📥 Creating recipe: " + recipe.getTitle());
//        System.out.println("👤 Author ID: " + (recipe.getAuthor() != null ? recipe.getAuthor().getId() : "null"));
//        Recipe saved = recipeService.createRecipe(recipe);
//        return ResponseEntity.ok(saved);
//    }
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        return ResponseEntity.ok(recipeService.createRecipe(recipe));
    }

//    @PostMapping("/{id}/fork")
//    public Recipe forkRecipe(@PathVariable Long id, @RequestBody User currentUser) {
//        return recipeService.forkRecipe(id, currentUser);
//    }

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    @GetMapping("/{id}")
    public Recipe getById(@PathVariable Long id) {
        return recipeService.getRecipeById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    @GetMapping("/public")
    public List<Recipe> getPublicRecipes() {
        return recipeService.getPublicRecipes();
    }

    @GetMapping("/user/{userId}")
    public List<Recipe> getUserRecipes(@PathVariable Long userId) {
        return recipeService.getRecipesByUserId(userId);
    }

    @GetMapping("/user/{userId}/public")
    public List<Recipe> getUserPublicRecipes(@PathVariable Long userId) {
        return recipeService.getPublicRecipesByUserId(userId);
    }

    @GetMapping("/user/{userId}/favourites")
    public List<Recipe> getUserFavourites(@PathVariable Long userId) {
        return recipeService.getFavoriteRecipesByUserId(userId);
    }

    @GetMapping("/user/{userId}/cooked")
    public List<Recipe> getUserCooked(@PathVariable Long userId) {
        return recipeService.getCookedRecipesByUserId(userId);
    }

    @GetMapping("/search")
    public List<Recipe> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String ingredient
    ) {
        if (title != null) {
            return recipeService.searchByTitle(title);
        } else if (ingredient != null) {
            return recipeService.searchByIngredient(ingredient);
        } else {
            return recipeService.getAllRecipes();
        }
    }
}
