package com.recipehub.backendrecipehub.dto;

import java.util.List;

public class AuthorSearchResponse {
    private Long authorId;
    private List<RecipeResponseDTO> recipes;
    private List<RecipeBookDTO> recipeBooks;
    private int totalRecipes;
    private int totalRecipeBooks;

    public AuthorSearchResponse() {}

    public AuthorSearchResponse(Long authorId, List<RecipeResponseDTO> recipes, List<RecipeBookDTO> recipeBooks) {
        this.authorId = authorId;
        this.recipes = recipes;
        this.recipeBooks = recipeBooks;
        this.totalRecipes = recipes != null ? recipes.size() : 0;
        this.totalRecipeBooks = recipeBooks != null ? recipeBooks.size() : 0;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public List<RecipeResponseDTO> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<RecipeResponseDTO> recipes) {
        this.recipes = recipes;
    }

    public List<RecipeBookDTO> getRecipeBooks() {
        return recipeBooks;
    }

    public void setRecipeBooks(List<RecipeBookDTO> recipeBooks) {
        this.recipeBooks = recipeBooks;
    }

    public int getTotalRecipes() {
        return totalRecipes;
    }

    public void setTotalRecipes(int totalRecipes) {
        this.totalRecipes = totalRecipes;
    }

    public int getTotalRecipeBooks() {
        return totalRecipeBooks;
    }

    public void setTotalRecipeBooks(int totalRecipeBooks) {
        this.totalRecipeBooks = totalRecipeBooks;
    }
}
