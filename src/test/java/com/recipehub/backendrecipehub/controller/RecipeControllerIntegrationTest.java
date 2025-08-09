package com.recipehub.backendrecipehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.recipehub.backendrecipehub.dto.IngredientDTO;
import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.UserRequestDTO;
import com.recipehub.backendrecipehub.model.User;
// removed unused TagService import
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
// removed unused RecipeService import
import com.recipehub.backendrecipehub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
// removed unused TestPropertySource import
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.context.annotation.Import;
import com.recipehub.backendrecipehub.config.TestConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class RecipeControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    // removed unused RecipeService field

    @Autowired
    private UserService userService;

    // removed unused TagService field

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Clean up and create test user
        // Clear join tables and self-references to avoid FK violations during cleanup
        jdbcTemplate.update("DELETE FROM recipe_tag");
        jdbcTemplate.update("DELETE FROM recipe_book_recipes");
        jdbcTemplate.update("UPDATE recipes SET original_recipe_id = NULL");
        recipeRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        
        // Use UserService to properly encode password
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        String suffix = String.valueOf(System.nanoTime());
        userRequestDTO.setUsername("testuser_" + suffix);
        userRequestDTO.setEmail("test_" + suffix + "@example.com");
        userRequestDTO.setPassword("password");
        userService.registerUser(userRequestDTO);
        
        // Get the created user from repository
        testUser = userRepository.findByUsernameAndDeletedFalse(userRequestDTO.getUsername()).orElse(null);
        
        // Tags are seeded via src/test/resources/data.sql
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
    void testForkRecipeWithAuthorAssignment() throws Exception {
        // Create a second test user
        UserRequestDTO secondUserRequest = new UserRequestDTO();
        String sfx2 = String.valueOf(System.nanoTime());
        secondUserRequest.setUsername("seconduser_" + sfx2);
        secondUserRequest.setEmail("second_" + sfx2 + "@example.com");
        secondUserRequest.setPassword("password");
        userService.registerUser(secondUserRequest);
        
        User secondUser = userRepository.findByUsernameAndDeletedFalse(secondUserRequest.getUsername()).orElse(null);
        
        // Create an original recipe
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Original Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(2.0);
        ingredients.add(ingredient);

        List<String> instructions = new ArrayList<>();
        instructions.add("Original instruction");

        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);

        MvcResult result = mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Original Recipe")
                .param("description", "Original Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the original recipe ID
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        Long originalRecipeId = responseJson.get("id").asLong();

        // Fork the recipe with author assignment
        RecipeRequestDTO forkRequest = new RecipeRequestDTO();
        forkRequest.setAuthorId(secondUser.getId());
        forkRequest.setTitle("Forked Recipe");

        mockMvc.perform(post("/api/recipes/" + originalRecipeId + "/fork")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Forked Recipe"))
                .andExpect(jsonPath("$.authorId").value(secondUser.getId()))
                .andExpect(jsonPath("$.authorUsername").value(secondUserRequest.getUsername()))
                .andExpect(jsonPath("$.originalRecipeId").value(originalRecipeId))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.tags").isArray());
    }

    @Test
    void testForkRecipeInheritsOriginalTags() throws Exception {
        // Create an original recipe with tags
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        List<String> instructions = new ArrayList<>();
        instructions.add("Test instruction");

        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);

        MvcResult result = mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Recipe with Tags")
                .param("description", "Test Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the original recipe ID
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        Long originalRecipeId = responseJson.get("id").asLong();

        // Fork the recipe without specifying tags (should inherit original tags)
        RecipeRequestDTO forkRequest = new RecipeRequestDTO();
        forkRequest.setAuthorId(testUser.getId());

        mockMvc.perform(post("/api/recipes/" + originalRecipeId + "/fork")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalRecipeId").value(originalRecipeId))
                .andExpect(jsonPath("$.tags").isArray());
    }

    @Test
    void testForkRecipeMinimalRequest() throws Exception {
        // Create an original recipe
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Original Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        List<String> instructions = new ArrayList<>();
        instructions.add("Original instruction");

        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);

        MvcResult result = mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Original Recipe")
                .param("description", "Original Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the original recipe ID
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        Long originalRecipeId = responseJson.get("id").asLong();

        // Fork with minimal request (just authorId)
        RecipeRequestDTO forkRequest = new RecipeRequestDTO();
        forkRequest.setAuthorId(testUser.getId());

        mockMvc.perform(post("/api/recipes/" + originalRecipeId + "/fork")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original Recipe (Forked)"))
                .andExpect(jsonPath("$.authorId").value(testUser.getId()))
                .andExpect(jsonPath("$.originalRecipeId").value(originalRecipeId))
                .andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    void testUpdateLikeCount() throws Exception {
        // Test with a non-existent ID first
        mockMvc.perform(put("/api/recipes/999/likecount")
                .param("likeCount", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateRecipeWithoutTagsThenAddTagsLater() throws Exception {
        // Step 1: Create a recipe without tags
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

        MvcResult result = mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Recipe Without Tags")
                .param("description", "Test Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags").isEmpty())
                .andReturn();

        // Extract the created recipe ID
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        Long recipeId = responseJson.get("id").asLong();

        // Step 2: Update the recipe to add tags
        RecipeRequestDTO updateRequest = new RecipeRequestDTO();
        updateRequest.setTitle("Recipe Without Tags"); // Keep the same title
        updateRequest.setDescription("Test Description"); // Keep the same description
        updateRequest.setIngredients(ingredients); // Keep the same ingredients
        updateRequest.setInstructions(instructions); // Keep the same instructions
        updateRequest.setTagNames(Arrays.asList("Italian", "Quick"));
        updateRequest.setAuthorId(testUser.getId());

        mockMvc.perform(put("/api/recipes/" + recipeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags").value(org.hamcrest.Matchers.hasItems("Italian", "Quick")));
    }

    @Test
    void testAddTagsToRecipe() throws Exception {
        // Create a recipe without tags
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        List<String> instructions = new ArrayList<>();
        instructions.add("Test instruction");

        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);

        MvcResult result = mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Recipe for Tag Testing")
                .param("description", "Test Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the recipe ID
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        Long recipeId = responseJson.get("id").asLong();

        // Add tags using tagsToAdd
        RecipeRequestDTO addTagsRequest = new RecipeRequestDTO();
        addTagsRequest.setAuthorId(testUser.getId());
        addTagsRequest.setTagsToAdd(Arrays.asList("Easy", "Quick"));

        mockMvc.perform(put("/api/recipes/" + recipeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addTagsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").value(org.hamcrest.Matchers.hasItems("Easy", "Quick")));
    }

    @Test
    void testRemoveTagsFromRecipe() throws Exception {
        // Create a recipe with tags
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        List<String> instructions = new ArrayList<>();
        instructions.add("Test instruction");

        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);

        MvcResult result = mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Recipe for Tag Removal")
                .param("description", "Test Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false")
                .param("tagNames", "[\"Easy\",\"Quick\",\"Healthy\"]"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the recipe ID
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        Long recipeId = responseJson.get("id").asLong();

        // Remove tags using tagsToDelete
        RecipeRequestDTO removeTagsRequest = new RecipeRequestDTO();
        removeTagsRequest.setAuthorId(testUser.getId());
        removeTagsRequest.setTagsToDelete(Arrays.asList("Quick"));

        mockMvc.perform(put("/api/recipes/" + recipeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(removeTagsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").value(org.hamcrest.Matchers.hasItems("Easy", "Healthy")))
                .andExpect(jsonPath("$.tags").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("Quick"))));
    }

    @Test
    void testAddAndRemoveTagsSimultaneously() throws Exception {
        // Create a recipe with initial tags
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        List<String> instructions = new ArrayList<>();
        instructions.add("Test instruction");

        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);

        MvcResult result = mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Recipe for Tag Management")
                .param("description", "Test Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false")
                .param("tagNames", "[\"Easy\",\"Quick\"]"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the recipe ID
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        Long recipeId = responseJson.get("id").asLong();

        // Add and remove tags simultaneously
        RecipeRequestDTO updateRequest = new RecipeRequestDTO();
        updateRequest.setAuthorId(testUser.getId());
        updateRequest.setTagsToAdd(Arrays.asList("Healthy", "Baked"));
        updateRequest.setTagsToDelete(Arrays.asList("Quick"));

        mockMvc.perform(put("/api/recipes/" + recipeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").value(org.hamcrest.Matchers.hasItems("Easy", "Healthy", "Baked")))
                .andExpect(jsonPath("$.tags").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("Quick"))));
    }

    @Test
    void testInvalidTagValidation() throws Exception {
        // Create a recipe
        List<IngredientDTO> ingredients = new ArrayList<>();
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("cup");
        ingredient.setQuantity(1.0);
        ingredients.add(ingredient);

        List<String> instructions = new ArrayList<>();
        instructions.add("Test instruction");

        String ingredientsJson = objectMapper.writeValueAsString(ingredients);
        String instructionsJson = objectMapper.writeValueAsString(instructions);

        MvcResult result = mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("title", "Recipe for Validation Test")
                .param("description", "Test Description")
                .param("ingredients", ingredientsJson)
                .param("instructions", instructionsJson)
                .param("authorId", testUser.getId().toString())
                .param("isPublic", "true")
                .param("cooked", "false")
                .param("favourite", "false"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the recipe ID
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        Long recipeId = responseJson.get("id").asLong();

        // Try to add invalid tag
        RecipeRequestDTO invalidRequest = new RecipeRequestDTO();
        invalidRequest.setAuthorId(testUser.getId());
        invalidRequest.setTagsToAdd(Arrays.asList("InvalidTag"));

        mockMvc.perform(put("/api/recipes/" + recipeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown tag: InvalidTag"));
    }
} 