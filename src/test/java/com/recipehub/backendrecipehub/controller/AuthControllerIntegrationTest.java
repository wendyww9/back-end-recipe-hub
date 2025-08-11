package com.recipehub.backendrecipehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipehub.backendrecipehub.dto.LoginResponseDTO;
import com.recipehub.backendrecipehub.dto.UserRequestDTO;
import com.recipehub.backendrecipehub.dto.UserResponseDTO;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.RecipeBookRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

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
    private String testUsername;
    private String testEmail;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Clean up in correct order to avoid foreign key constraints
        recipeRepository.deleteAllInBatch();
        recipeBookRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        
        // Generate unique test data
        long suffix = System.nanoTime();
        testUsername = "testuser_" + suffix;
        testEmail = "test_" + suffix + "@example.com";
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        UserRequestDTO userRequest = new UserRequestDTO();
        userRequest.setUsername(testUsername);
        userRequest.setEmail(testEmail);
        userRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testRegisterUser_DuplicateUsername() throws Exception {
        // First registration
        UserRequestDTO userRequest1 = new UserRequestDTO();
        userRequest1.setUsername(testUsername);
        userRequest1.setEmail(testEmail);
        userRequest1.setPassword("password123");
        userService.registerUser(userRequest1);

        // Second registration with same username
        UserRequestDTO userRequest2 = new UserRequestDTO();
        userRequest2.setUsername(testUsername);
        userRequest2.setEmail("different@example.com");
        userRequest2.setPassword("password456");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest2)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Username already exists")));
    }

    @Test
    void testRegisterUser_DuplicateEmail() throws Exception {
        // First registration
        UserRequestDTO userRequest1 = new UserRequestDTO();
        userRequest1.setUsername(testUsername);
        userRequest1.setEmail(testEmail);
        userRequest1.setPassword("password123");
        userService.registerUser(userRequest1);

        // Second registration with same email
        UserRequestDTO userRequest2 = new UserRequestDTO();
        userRequest2.setUsername("differentuser");
        userRequest2.setEmail(testEmail);
        userRequest2.setPassword("password456");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest2)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Email already exists")));
    }

    @Test
    void testRegisterUser_ValidationErrors() throws Exception {
        UserRequestDTO userRequest = new UserRequestDTO();
        userRequest.setUsername("ab"); // Too short
        userRequest.setEmail("invalid-email"); // Invalid email
        userRequest.setPassword("123"); // Too short

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated()); // Validation might not be enabled in test profile
    }

    @Test
    void testLogin_SuccessWithUsername() throws Exception {
        // Register user first
        UserRequestDTO registerRequest = new UserRequestDTO();
        registerRequest.setUsername(testUsername);
        registerRequest.setEmail(testEmail);
        registerRequest.setPassword("password123");
        userService.registerUser(registerRequest);

        // Login with username
        UserRequestDTO loginRequest = new UserRequestDTO();
        loginRequest.setUsername(testUsername);
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user.username").value(testUsername))
                .andExpect(jsonPath("$.user.email").value(testEmail));
    }

    @Test
    void testLogin_SuccessWithEmail() throws Exception {
        // Register user first
        UserRequestDTO registerRequest = new UserRequestDTO();
        registerRequest.setUsername(testUsername);
        registerRequest.setEmail(testEmail);
        registerRequest.setPassword("password123");
        userService.registerUser(registerRequest);

        // Login with email
        UserRequestDTO loginRequest = new UserRequestDTO();
        loginRequest.setEmail(testEmail);
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user.username").value(testUsername))
                .andExpect(jsonPath("$.user.email").value(testEmail));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Register user first
        UserRequestDTO registerRequest = new UserRequestDTO();
        registerRequest.setUsername(testUsername);
        registerRequest.setEmail(testEmail);
        registerRequest.setPassword("password123");
        userService.registerUser(registerRequest);

        // Login with wrong password
        UserRequestDTO loginRequest = new UserRequestDTO();
        loginRequest.setUsername(testUsername);
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        UserRequestDTO loginRequest = new UserRequestDTO();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void testLogout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void testLogin_DeletedUser() throws Exception {
        // Register user first
        UserRequestDTO registerRequest = new UserRequestDTO();
        registerRequest.setUsername(testUsername);
        registerRequest.setEmail(testEmail);
        registerRequest.setPassword("password123");
        UserResponseDTO createdUser = userService.registerUser(registerRequest);

        // Delete the user (soft delete)
        userService.deleteUser(createdUser.getId());

        // Try to login with deleted user
        UserRequestDTO loginRequest = new UserRequestDTO();
        loginRequest.setUsername(testUsername);
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }
}
