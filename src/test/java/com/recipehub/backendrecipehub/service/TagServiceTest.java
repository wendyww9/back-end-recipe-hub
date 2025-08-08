package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.TagDTO;
import com.recipehub.backendrecipehub.exception.DuplicateResourceException;
import com.recipehub.backendrecipehub.exception.UserNotFoundException;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.Tag;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.TagRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@org.springframework.context.annotation.Import(com.recipehub.backendrecipehub.config.TestConfig.class)
class TagServiceTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RecipeService recipeService;

    private User testUser;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        // Clean up
        recipeRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // Create test tag
        testTag = new Tag();
        testTag.setName("TestTag");
        testTag = tagRepository.save(testTag);
    }

    @Test
    void testFindAllTags() {
        List<Tag> tags = tagService.findAllTags();
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        assertTrue(tags.stream().anyMatch(tag -> tag.getName().equals("TestTag")));
    }

    @Test
    void testFindAllTagsWithRecipeCount() {
        List<TagDTO> tags = tagService.findAllTagsWithRecipeCount();
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        assertTrue(tags.stream().anyMatch(tag -> tag.getName().equals("TestTag")));
    }

    @Test
    void testGetTagByName() {
        Tag tag = tagService.getTagByName("TestTag");
        assertNotNull(tag);
        assertEquals("TestTag", tag.getName());
    }

    @Test
    void testGetTagByNameNotFound() {
        assertThrows(RuntimeException.class, () -> {
            tagService.getTagByName("NonExistentTag");
        });
    }

    // Removed initializePredefinedTags tests; tags are seeded via src/test/resources/data.sql

    @Test
    void testGetPopularTags() {
        // Create some recipes with tags to make them popular
        createRecipeWithTag("Italian");
        createRecipeWithTag("Italian"); // Make Italian more popular
        createRecipeWithTag("Quick");
        
        List<TagDTO> popularTags = tagService.getPopularTags(5);
        assertNotNull(popularTags);
        assertFalse(popularTags.isEmpty());
        assertTrue(popularTags.size() <= 5);
    }

    @Test
    void testGetPopularTagsWithDefaultLimit() {
        List<TagDTO> popularTags = tagService.getPopularTags(10);
        assertNotNull(popularTags);
        assertFalse(popularTags.isEmpty());
        assertTrue(popularTags.size() <= 10);
    }

    @Test
    void testGetCategoryTags() {
        List<TagDTO> categoryTags = tagService.getCategoryTags();
        assertNotNull(categoryTags);
        assertFalse(categoryTags.isEmpty());
        
        // Should contain main category tags
        List<String> tagNames = categoryTags.stream()
                .map(TagDTO::getName)
                .toList();
        
        assertTrue(tagNames.contains("Italian") || 
                  tagNames.contains("Breakfast") || 
                  tagNames.contains("Vegetarian") ||
                  tagNames.contains("Easy"));
    }

    @Test
    void testGetTagsByNames() {
        // Create some tags manually
        Tag tag1 = new Tag();
        tag1.setName("Tag1");
        tagRepository.save(tag1);
        
        Tag tag2 = new Tag();
        tag2.setName("Tag2");
        tagRepository.save(tag2);
        
        // Test getting tags by names using available repository methods
        List<String> tagNames = Arrays.asList("TestTag", "Tag1", "Tag2", "NonExistentTag");
        List<Tag> tags = new ArrayList<>();
        
        for (String name : tagNames) {
            Optional<Tag> tag = tagRepository.findByNameIgnoreCase(name);
            tag.ifPresent(tags::add);
        }
        
        assertEquals(3, tags.size()); // Should only return existing tags
        assertTrue(tags.stream().anyMatch(tag -> tag.getName().equals("TestTag")));
        assertTrue(tags.stream().anyMatch(tag -> tag.getName().equals("Tag1")));
        assertTrue(tags.stream().anyMatch(tag -> tag.getName().equals("Tag2")));
    }

    private void createRecipeWithTag(String tagName) {
        // Create tag if it doesn't exist
        Tag tag = tagRepository.findByNameIgnoreCase(tagName).orElseGet(() -> {
            Tag newTag = new Tag();
            newTag.setName(tagName);
            return tagRepository.save(newTag);
        });

        // Create recipe with tag
        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe with " + tagName);
        recipe.setDescription("Test Description");
        recipe.setAuthor(testUser);
        recipe.setPublic(true);
        recipe.setCooked(false);
        recipe.setFavourite(false);
        recipe.setTags(Arrays.asList(tag));
        recipe.setIngredients(Arrays.asList());
        recipe.setInstructions(Arrays.asList("Test instruction"));

        recipeRepository.save(recipe);
    }
} 