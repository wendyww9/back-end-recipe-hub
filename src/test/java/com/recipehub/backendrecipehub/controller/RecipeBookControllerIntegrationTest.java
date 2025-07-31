package com.recipehub.backendrecipehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipehub.backendrecipehub.dto.RecipeBookDTO;
import com.recipehub.backendrecipehub.dto.UserDTO;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.RecipeBookRepository;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.service.RecipeBookService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class RecipeBookControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RecipeBookRepository recipeBookRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeBookService recipeBookService;

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
        recipeBookRepository.deleteAll();
        recipeRepository.deleteAll();
        userRepository.deleteAll();
        
        // Use UserService to properly encode password
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");
        testUser = userService.registerUser(userDTO);
    }

    @Test
    void testGetAllRecipeBooks() throws Exception {
        mockMvc.perform(get("/api/recipebooks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetAllPublicRecipeBooks() throws Exception {
        mockMvc.perform(get("/api/recipebooks/public"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateRecipeBook() throws Exception {
        RecipeBookDTO recipeBookRequest = new RecipeBookDTO();
        recipeBookRequest.setName("Test Recipe Book");
        recipeBookRequest.setDescription("Test Description");
        recipeBookRequest.setIsPublic(true);
        recipeBookRequest.setUserId(testUser.getId());
        recipeBookRequest.setRecipeIds(new ArrayList<>());

        mockMvc.perform(post("/api/recipebooks")
                .param("userId", testUser.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recipeBookRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRecipeBookById() throws Exception {
        // Test with a non-existent ID first
        mockMvc.perform(get("/api/recipebooks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteRecipeBook() throws Exception {
        // Create a recipe book first
        RecipeBookDTO recipeBookRequest = new RecipeBookDTO();
        recipeBookRequest.setName("Test Recipe Book");
        recipeBookRequest.setDescription("Test Description");
        recipeBookRequest.setIsPublic(true);
        recipeBookRequest.setUserId(testUser.getId());
        recipeBookRequest.setRecipeIds(new ArrayList<>());

        // Create the recipe book
        RecipeBookDTO createdBook = recipeBookService.createRecipeBook(recipeBookRequest, testUser.getId());

        // Delete the recipe book
        mockMvc.perform(delete("/api/recipebooks/" + createdBook.getId())
                .param("userId", testUser.getId().toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetUserRecipeBooks() throws Exception {
        // Create a recipe book for the test user
        RecipeBookDTO recipeBookRequest = new RecipeBookDTO();
        recipeBookRequest.setName("Test Recipe Book");
        recipeBookRequest.setDescription("Test Description");
        recipeBookRequest.setIsPublic(true);
        recipeBookRequest.setUserId(testUser.getId());
        recipeBookRequest.setRecipeIds(new ArrayList<>());

        // Create the recipe book
        recipeBookService.createRecipeBook(recipeBookRequest, testUser.getId());

        // Test getting user's recipe books
        mockMvc.perform(get("/api/users/" + testUser.getId() + "/recipe-books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Recipe Book"))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()));
    }
} 