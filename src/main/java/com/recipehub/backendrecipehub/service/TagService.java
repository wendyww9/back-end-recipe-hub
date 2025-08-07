package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.TagDTO;
import com.recipehub.backendrecipehub.model.Tag;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final RecipeRepository recipeRepository;

    @Autowired
    public TagService(TagRepository tagRepository, RecipeRepository recipeRepository) {
        this.tagRepository = tagRepository;
        this.recipeRepository = recipeRepository;
    }

    public List<Tag> findAllTags() {
        return tagRepository.findAll();
    }

    public List<TagDTO> findAllTagsWithRecipeCount() {
        return tagRepository.findAllWithRecipeCount();
    }

    public Tag getTagByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + name));
    }

    public void initializePredefinedTags() {
        // Check if tags already exist
        if (!tagRepository.findAll().isEmpty()) {
            return; // Tags already initialized
        }

        // Predefined tags organized by categories
        List<String> predefinedTags = Arrays.asList(
            // Cuisine Types
            "Italian", "Mexican", "Asian", "French", "Indian", "Mediterranean", "American", "Thai", "Japanese", "Chinese",
            
            // Meal Types
            "Breakfast", "Lunch", "Dinner", "Dessert", "Appetizer", "Snack", "Brunch", "Late Night",
            
            // Dietary Preferences
            "Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Low-Carb", "Keto", "Paleo", "Halal", "Kosher",
            
            // Cooking Methods
            "Baked", "Grilled", "Fried", "Steamed", "Roasted", "Slow Cooker", "Instant Pot", "Air Fryer", "Smoked",
            
            // Difficulty Levels
            "Easy", "Medium", "Hard", "Beginner", "Advanced", "Quick", "30-Minute Meals",
            
            // Occasions
            "Holiday", "Birthday", "Anniversary", "Party", "Date Night", "Family Dinner", "Potluck", "Picnic",
            
            // Seasons
            "Spring", "Summer", "Fall", "Winter", "Seasonal",
            
            // Health & Wellness
            "Healthy", "Low-Calorie", "High-Protein", "Low-Sodium", "Heart-Healthy", "Anti-Inflammatory",
            
            // Ingredient Types
            "Chicken", "Beef", "Pork", "Fish", "Seafood", "Pasta", "Rice", "Vegetables", "Fruits", "Nuts", "Cheese",
            
            // Special Features
            "One-Pot", "Make-Ahead", "Freezer-Friendly", "Kid-Friendly", "Crowd-Pleaser", "Comfort Food", "Gourmet"
        );

        for (String tagName : predefinedTags) {
            Tag tag = new Tag();
            tag.setName(tagName);
            tagRepository.save(tag);
        }
    }

    public List<TagDTO> getPopularTags(int limit) {
        List<TagDTO> allTags = tagRepository.findAllWithRecipeCount();
        return allTags.stream()
                .sorted((t1, t2) -> Integer.compare(t2.getRecipeCount(), t1.getRecipeCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<TagDTO> getCategoryTags() {
        // Return main category tags for browsing
        List<String> categoryTags = Arrays.asList(
            "Italian", "Mexican", "Asian", "French", "Indian", "Mediterranean", "American",
            "Breakfast", "Lunch", "Dinner", "Dessert",
            "Vegetarian", "Vegan", "Gluten-Free",
            "Easy", "Quick", "Healthy"
        );
        
        return categoryTags.stream()
                .map(tagName -> {
                    try {
                        Tag tag = getTagByName(tagName);
                        return new TagDTO(tag.getId(), tag.getName(), tag.getRecipes().size());
                    } catch (RuntimeException e) {
                        return new TagDTO(null, tagName, 0);
                    }
                })
                .collect(Collectors.toList());
    }
}
