package com.recipehub.backendrecipehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipehub.backendrecipehub.dto.IngredientDTO;
import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.UserRequestDTO;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.service.RecipeService;
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
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class RecipeControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Clean up and create test user
        recipeRepository.deleteAll();
        userRepository.deleteAll();
        
        // Use UserService to properly encode password
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("testuser");
        userRequestDTO.setEmail("test@example.com");
        userRequestDTO.setPassword("password");
        userService.registerUser(userRequestDTO);
        
        // Get the created user from repository
        testUser = userRepository.findByUsername("testuser").orElse(null);
    }

    @Test
    void testGetAllRecipes() throws Exception {
        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetAllPublicRecipes() throws Exception {
        mockMvc.perform(get("/api/recipes/public"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testSearchByTitle() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("title", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateRecipe() throws Exception {
        // Create valid ingredients JSON
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        // Create valid instructions JSON
        List<String> instructions = new ArrayList<>();
        instructions.add("Test instruction step 1");

        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Test Recipe")
                .param("description", "Test Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetRecipeById() throws Exception {
        // Test with a non-existent ID first
        mockMvc.perform(get("/api/recipes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateRecipeWithImage() throws Exception {
        // Create valid ingredients JSON
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        // Create valid instructions JSON
        List<String> instructions = new ArrayList<>();
        instructions.add("Test instruction step 1");

        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);

        // Create a mock image file
        MockMultipartFile imageFile = new MockMultipartFile(
            "file", 
            "test-image.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/recipes")
                .file(imageFile)
                .param("title", "Test Recipe with Image")
                .param("description", "Test Description with Image")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testUploadRecipeImage() throws Exception {
        // Create a mock image file
        MockMultipartFile imageFile = new MockMultipartFile(
            "file", 
            "test-image.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );

        // Test with a non-existent recipe ID
        mockMvc.perform(multipart("/api/recipes/999/image")
                .file(imageFile))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteRecipeImage() throws Exception {
        // Test with a non-existent recipe ID
        mockMvc.perform(delete("/api/recipes/999/image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testForkRecipe() throws Exception {
        // Test with a non-existent recipe ID
        mockMvc.perform(post("/api/recipes/999/fork")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateLikeCount() throws Exception {
        // Test with a non-existent ID first
        mockMvc.perform(put("/api/recipes/999/likecount")
                .param("likeCount", "5"))
                .andExpect(status().isNotFound());
    }
} 