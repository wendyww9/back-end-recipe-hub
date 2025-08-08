package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.TagDTO;
import com.recipehub.backendrecipehub.exception.ValidationException;
import com.recipehub.backendrecipehub.model.Tag;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
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
        return tagRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + name));
    }

    public List<Tag> resolveTagsByName(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
    
        // ✅ Validate no duplicates (case-insensitive)
        Set<String> normalizedSet = new HashSet<>();
        for (String name : tagNames) {
            String normalized = name.trim().toLowerCase();
            if (!normalizedSet.add(normalized)) {
                throw new ValidationException("Duplicate tag name: " + name);
            }
        }
    
        // ✅ Lookup existing tags by name (case-insensitive)
        List<Tag> resolvedTags = new ArrayList<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                    .orElseThrow(() -> new ValidationException("Unknown tag: " + tagName));
            resolvedTags.add(tag);
        }
    
        return resolvedTags;
    }

    // Removed initializePredefinedTags: tags are seeded via test data.sql and managed externally in prod

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
