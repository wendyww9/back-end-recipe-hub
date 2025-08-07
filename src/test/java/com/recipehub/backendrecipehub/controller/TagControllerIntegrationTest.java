package com.recipehub.backendrecipehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipehub.backendrecipehub.dto.IngredientDTO;
import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.UserRequestDTO;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.Tag;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.RecipeBookRepository;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.TagRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.service.RecipeService;
import com.recipehub.backendrecipehub.service.TagService;
import com.recipehub.backendrecipehub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class TagControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private RecipeBookRepository recipeBookRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Clean up in correct order to avoid foreign key constraint violations
        recipeRepository.deleteAll();
        recipeBookRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("testuser");
        userRequestDTO.setEmail("test@example.com");
        userRequestDTO.setPassword("password");
        userService.registerUser(userRequestDTO);
        
        testUser = userRepository.findByUsername("testuser").orElse(null);
        
        // Create test tag
        testTag = new Tag();
        testTag.setName("TestTag");
        testTag = tagRepository.save(testTag);
    }



    @Test
    void testGetTagByName() throws Exception {
        mockMvc.perform(get("/api/tags/{name}", testTag.getName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(testTag.getName()));
    }

    @Test
    void testGetTagByNameNotFound() throws Exception {
        mockMvc.perform(get("/api/tags/NonExistentTag"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testInitializePredefinedTags() throws Exception {
        // Clear existing tags first
        tagRepository.deleteAll();
        
        mockMvc.perform(post("/api/tags/initialize"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Predefined tags initialized successfully"));
    }

    @Test
    void testInitializePredefinedTagsAlreadyExists() throws Exception {
        // Tags already exist from setUp()
        mockMvc.perform(post("/api/tags/initialize"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Predefined tags initialized successfully"));
    }

    @Test
    void testGetPopularTags() throws Exception {
        mockMvc.perform(get("/api/tags/popular")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetPopularTagsWithDefaultLimit() throws Exception {
        mockMvc.perform(get("/api/tags/popular"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetCategoryTags() throws Exception {
        mockMvc.perform(get("/api/tags/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAllTags() throws Exception {
        mockMvc.perform(get("/api/tags/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testSearchRecipesByTag() throws Exception {
        // Create a recipe with a specific tag
        String tagName = "Italian";
        createRecipeWithTag(tagName);
        
        mockMvc.perform(get("/api/recipes/search")
                .param("tags", tagName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testSearchRecipesByMultipleTags() throws Exception {
        // Create recipes with different tags
        createRecipeWithTag("Italian");
        createRecipeWithTag("Quick");
        
        mockMvc.perform(get("/api/recipes/search")
                .param("tags", "Italian,Quick"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testSearchRecipesByTagCategory() throws Exception {
        // Create a recipe with cuisine tag
        createRecipeWithTag("Italian");
        
        mockMvc.perform(get("/api/recipes/search")
                .param("cuisine", "Italian"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testSearchRecipesByMultipleCriteria() throws Exception {
        // Create a recipe with multiple criteria
        createRecipeWithTag("Italian");
        
        mockMvc.perform(get("/api/recipes/search")
                .param("cuisine", "Italian")
                .param("difficulty", "Easy")
                .param("isPublic", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreateRecipeWithTags() throws Exception {
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        List<String> instructions = new ArrayList<>();
        instructions.add("Test instruction step 1");

        // Convert to JSON strings for multipart form
        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);
        String tagNamesJson = objectMapper.writeValueAsString(Arrays.asList("Italian", "Quick", "Easy"));

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Test Recipe with Tags")
                .param("description", "Test Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false")
                .param("tagNames", tagNamesJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags").value(org.hamcrest.Matchers.hasItems("Italian", "Quick", "Easy")));
    }

    private void createRecipeWithTag(String tagName) {
        // Create tag if it doesn't exist
        Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
            Tag newTag = new Tag();
            newTag.setName(tagName);
            return tagRepository.save(newTag);
        });

        // Create recipe with tag
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        List<String> instructions = new ArrayList<>();
        instructions.add("Test instruction step 1");

        RecipeRequestDTO recipeRequest = new RecipeRequestDTO();
        recipeRequest.setTitle("Test Recipe with " + tagName);
        recipeRequest.setDescription("Test Description");
        recipeRequest.setAuthorId(testUser.getId());
        recipeRequest.setIsPublic(true);
        recipeRequest.setCooked(false);
        recipeRequest.setFavourite(false);
        recipeRequest.setIngredients(ingredients);
        recipeRequest.setInstructions(instructions);
        recipeRequest.setTagNames(Arrays.asList(tagName));

        recipeService.createRecipeWithValidation(recipeRequest);
    }
} 