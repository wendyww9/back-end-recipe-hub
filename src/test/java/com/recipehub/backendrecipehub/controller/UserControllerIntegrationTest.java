package com.recipehub.backendrecipehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipehub.backendrecipehub.dto.UserRequestDTO;
import com.recipehub.backendrecipehub.dto.UserUpdateDTO;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.RecipeBookRepository;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.RecipeBook;
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


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeBookRepository recipeBookRepository;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private String testUsername;
    private String testEmail;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Clean up and create test user (hard delete to avoid soft-delete constraints)
        recipeBookRepository.deleteAllInBatch();
        recipeRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        
        // Use UserService to properly encode password
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        long suffix = System.nanoTime();
        testUsername = "testuser_" + suffix;
        testEmail = "test_" + suffix + "@example.com";
        userRequestDTO.setUsername(testUsername);
        userRequestDTO.setEmail(testEmail);
        userRequestDTO.setPassword("password");
        userService.registerUser(userRequestDTO);
        
        // Get the created user from repository
        testUser = userRepository.findByUsernameAndDeletedFalse(testUsername).orElse(null);
    }

    @Test
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.email").value(testEmail));
    }

    @Test
    void testGetUserRecipes() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId() + "/recipes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetUserCookedRecipes() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId() + "/recipes/cooked"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetUserFavouriteRecipes() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId() + "/recipes/favourite"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testDeleteUserIsSoft_andKeepsRecipesAndBooks() throws Exception {
        // Create a recipe owned by user
        Recipe recipe = Recipe.builder()
                .author(testUser)
                .title("Soft Delete Recipe")
                .isPublic(true)
                .cooked(false)
                .favourite(false)
                .likeCount(0)
                .build();
        recipe = recipeRepository.save(recipe);

        // Create a recipe book owned by user containing the recipe
        RecipeBook book = RecipeBook.builder()
                .name("SoftDel Book")
                .description("Test book")
                .isPublic(true)
                .user(testUser)
                .build();
        book.getRecipes().add(recipe);
        book = recipeBookRepository.save(book);

        Long uid = testUser.getId();
        Long rid = recipe.getId();
        Long bid = book.getId();

        // Delete user via controller
        mockMvc.perform(delete("/api/users/" + uid))
                .andExpect(status().isOk());

        // User should 404
        mockMvc.perform(get("/api/users/" + uid))
                .andExpect(status().isNotFound());

        // Recipe and RecipeBook still present
        mockMvc.perform(get("/api/recipes/" + rid))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/recipebooks/" + bid))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdatePassword() throws Exception {
        UserUpdateDTO passwordUpdate = new UserUpdateDTO();
        passwordUpdate.setCurrentPassword("password");
        passwordUpdate.setNewPassword("newpassword");

        mockMvc.perform(put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordUpdate)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateEmail() throws Exception {
        UserUpdateDTO emailUpdate = new UserUpdateDTO();
        emailUpdate.setCurrentPassword("password");
        emailUpdate.setEmail("newemail@example.com");

        mockMvc.perform(put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailUpdate)))
                .andExpect(status().isOk());
    }
} 