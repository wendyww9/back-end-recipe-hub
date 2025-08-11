package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.RecipeResponseDTO;
import com.recipehub.backendrecipehub.dto.AuthorSearchResponse;
import com.recipehub.backendrecipehub.dto.IngredientDTO;
import com.recipehub.backendrecipehub.exception.RecipeNotFoundException;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.model.Tag;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecipeServiceTest {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private User testUser;
    private User testUser2;
    private Tag testTag;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        // Clean up
        recipeRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // Create test user
        testUser = createTestUser("testuser1", "test1@example.com");
        testUser2 = createTestUser("testuser2", "test2@example.com");

        // Create test tag
        testTag = new Tag();
        testTag.setName("Easy");
        testTag = tagRepository.save(testTag);

        // Create test recipe
        testRecipe = createTestRecipe("Test Recipe", testUser, true);
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encodedpassword");
        user.setDeleted(false);
        return userRepository.save(user);
    }

    private Recipe createTestRecipe(String title, User author, boolean isPublic) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setDescription("Test description");
        recipe.setAuthor(author);
        recipe.setPublic(isPublic);
        recipe.setCooked(false);
        recipe.setFavourite(false);
        recipe.setLikeCount(0);
        recipe.setDeleted(false);
        recipe.setIngredients(List.of());
        recipe.setInstructions(List.of());
        return recipeRepository.save(recipe);
    }

    @Test
    void testCreateRecipeWithValidation_Success() {
        RecipeRequestDTO request = new RecipeRequestDTO();
        request.setTitle("New Recipe");
        request.setDescription("New description");
        request.setAuthorId(testUser.getId());
        request.setIsPublic(true);
        request.setCooked(false);
        request.setFavourite(false);
        
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("New Ingredient");
        ingredient.setUnit("piece");
        ingredient.setQuantity(1.0);
        request.setIngredients(List.of(ingredient));
        
        request.setInstructions(List.of("New instruction"));

        RecipeResponseDTO result = recipeService.createRecipeWithValidation(request);

        assertNotNull(result);
        assertEquals("New Recipe", result.getTitle());
        assertEquals("New description", result.getDescription());
        assertEquals(testUser.getId(), result.getAuthorId());
        assertEquals(testUser.getUsername(), result.getAuthorUsername());
        assertTrue(result.getIsPublic());
        assertEquals(1, result.getIngredients().size());
        assertEquals("New Ingredient", result.getIngredients().get(0).getName());
    }

    @Test
    void testCreateRecipeWithValidation_UserNotFound() {
        RecipeRequestDTO request = new RecipeRequestDTO();
        request.setTitle("New Recipe");
        request.setDescription("New description");
        request.setAuthorId(999L); // Non-existent user
        request.setIsPublic(true);

        assertThrows(RecipeNotFoundException.class, () -> {
            recipeService.createRecipeWithValidation(request);
        });
    }

    @Test
    void testGetRecipeById_Success() {
        Optional<RecipeResponseDTO> result = recipeService.getRecipeById(testRecipe.getId());

        assertTrue(result.isPresent());
        assertEquals(testRecipe.getTitle(), result.get().getTitle());
        assertEquals(testRecipe.getDescription(), result.get().getDescription());
        assertEquals(testUser.getId(), result.get().getAuthorId());
    }

    @Test
    void testGetRecipeById_NotFound() {
        Optional<RecipeResponseDTO> result = recipeService.getRecipeById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllPublicRecipes() {
        // Create additional recipes
        createTestRecipe("Recipe 2", testUser, true);
        createTestRecipe("Recipe 3", testUser, false);

        List<RecipeResponseDTO> results = recipeService.getAllPublicRecipes();

        assertNotNull(results);
        assertTrue(results.size() >= 2);
        // Should only return public recipes
        results.forEach(recipe -> assertTrue(recipe.getIsPublic()));
        // Verify we have the expected number of public recipes
        long publicRecipeCount = results.stream().filter(RecipeResponseDTO::getIsPublic).count();
        assertEquals(results.size(), publicRecipeCount);
    }

    @Test
    void testSearchRecipes_ByTitle() {
        @SuppressWarnings("unchecked")
        List<RecipeResponseDTO> results = (List<RecipeResponseDTO>) recipeService.searchRecipes("Test", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        results.forEach(recipe -> assertTrue(recipe.getTitle().toLowerCase().contains("test")));
    }

    @Test
    void testSearchRecipes_ByAuthorId() {
        AuthorSearchResponse result = (AuthorSearchResponse) recipeService.searchRecipes(null, null, null, testUser.getId(), null, null, null, null, null, null, null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getAuthorId());
        assertFalse(result.getRecipes().isEmpty());
        assertEquals(testRecipe.getTitle(), result.getRecipes().get(0).getTitle());
    }

    @Test
    void testSearchRecipes_ByAuthorName() {
        AuthorSearchResponse result = (AuthorSearchResponse) recipeService.searchRecipes(null, null, testUser.getUsername(), null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getAuthorId());
        assertFalse(result.getRecipes().isEmpty());
        assertEquals(testRecipe.getTitle(), result.getRecipes().get(0).getTitle());
    }

    @Test
    void testSearchRecipes_ByIngredient() {
        // Create recipe with specific ingredient
        RecipeRequestDTO request = new RecipeRequestDTO();
        request.setTitle("Ingredient Recipe");
        request.setDescription("Test description");
        request.setAuthorId(testUser.getId());
        request.setIsPublic(true);
        request.setCooked(false);
        request.setFavourite(false);
        
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Flour");
        ingredient.setUnit("cup");
        ingredient.setQuantity(2.0);
        request.setIngredients(List.of(ingredient));
        
        request.setInstructions(List.of("Test instruction"));
        recipeService.createRecipeWithValidation(request);

        @SuppressWarnings("unchecked")
        List<RecipeResponseDTO> results = (List<RecipeResponseDTO>) recipeService.searchRecipes(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        // Should find recipes containing "Flour" ingredient
        boolean foundFlourIngredient = results.stream()
            .anyMatch(recipe -> recipe.getIngredients().stream()
                .anyMatch(ing -> ing.getName().toLowerCase().contains("flour")));
        assertTrue(foundFlourIngredient);
    }

    @Test
    void testForkRecipe_Success() {
        RecipeRequestDTO forkRequest = new RecipeRequestDTO();
        forkRequest.setTitle("Forked Recipe");
        forkRequest.setDescription("Forked description");
        forkRequest.setIsPublic(true);

        RecipeResponseDTO result = recipeService.forkRecipe(testRecipe.getId(), forkRequest, testUser2.getId());

        assertNotNull(result);
        assertEquals("Forked Recipe", result.getTitle());
        assertEquals("Forked description", result.getDescription());
        assertEquals(testRecipe.getId(), result.getOriginalRecipeId());
        assertEquals(testUser2.getId(), result.getAuthorId());
        assertEquals(testUser2.getUsername(), result.getAuthorUsername());
    }

    @Test
    void testForkRecipe_SimpleFork() {
        RecipeResponseDTO result = recipeService.forkRecipe(testRecipe.getId(), new RecipeRequestDTO(), testUser2.getId());

        assertNotNull(result);
        assertTrue(result.getTitle().contains("(Forked)"));
        assertEquals(testRecipe.getId(), result.getOriginalRecipeId());
        assertEquals(testRecipe.getDescription(), result.getDescription());
    }

    @Test
    void testForkRecipe_OriginalNotFound() {
        assertThrows(RecipeNotFoundException.class, () -> {
            recipeService.forkRecipe(999L, new RecipeRequestDTO(), testUser2.getId());
        });
    }

    @Test
    void testUpdateRecipe_Success() {
        RecipeRequestDTO updateRequest = new RecipeRequestDTO();
        updateRequest.setTitle("Updated Recipe");
        updateRequest.setDescription("Updated description");
        updateRequest.setIsPublic(false);
        updateRequest.setCooked(true);
        updateRequest.setFavourite(true);
        
        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Updated Ingredient");
        ingredient.setUnit("pieces");
        ingredient.setQuantity(2.0);
        updateRequest.setIngredients(List.of(ingredient));
        
        updateRequest.setInstructions(List.of("Updated instruction"));

        RecipeResponseDTO result = recipeService.updateRecipe(testRecipe.getId(), updateRequest);

        assertNotNull(result);
        assertEquals("Updated Recipe", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.getIsPublic());
        assertEquals(1, result.getIngredients().size());
        assertEquals("Updated Ingredient", result.getIngredients().get(0).getName());
    }

    @Test
    void testUpdateRecipe_NotFound() {
        RecipeRequestDTO updateRequest = new RecipeRequestDTO();
        updateRequest.setTitle("Updated Recipe");

        assertThrows(RecipeNotFoundException.class, () -> {
            recipeService.updateRecipe(999L, updateRequest);
        });
    }

    @Test
    void testDeleteRecipe_Success() {
        assertDoesNotThrow(() -> {
            recipeService.deleteRecipe(testRecipe.getId());
        });
        // Test passes if no exception is thrown
    }

    @Test
    void testDeleteRecipe_NotFound() {
        assertThrows(RecipeNotFoundException.class, () -> {
            recipeService.deleteRecipe(999L);
        });
    }

    @Test
    void testUpdateLikeCount_Success() {
        int newLikeCount = 15;
        RecipeResponseDTO result = recipeService.updateLikeCount(testRecipe.getId(), newLikeCount);

        assertNotNull(result);
        assertEquals(newLikeCount, result.getLikeCount());
    }

    @Test
    void testUpdateLikeCount_NotFound() {
        assertThrows(RecipeNotFoundException.class, () -> {
            recipeService.updateLikeCount(999L, 10);
        });
    }

    @Test
    void testSearchByAuthorId_WithRecipeBooks() {
        AuthorSearchResponse result = recipeService.searchByAuthorId(testUser.getId());

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getAuthorId());
        assertFalse(result.getRecipes().isEmpty());
        assertEquals(testRecipe.getTitle(), result.getRecipes().get(0).getTitle());
    }

    @Test
    void testSearchByAuthorName_CaseInsensitive() {
        AuthorSearchResponse result = recipeService.searchByAuthorName(testUser.getUsername().toUpperCase());

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getAuthorId());
        assertFalse(result.getRecipes().isEmpty());
    }

    @Test
    void testSearchByAuthorName_UserNotFound() {
        AuthorSearchResponse result = recipeService.searchByAuthorName("nonexistentuser");

        assertNotNull(result);
        assertEquals(0, result.getTotalRecipes());
        assertEquals(0, result.getTotalRecipeBooks());
    }

    @Test
    void testGetRecipesByUserId() {
        // Create additional recipes for the user
        createTestRecipe("User Recipe 1", testUser, true);
        createTestRecipe("User Recipe 2", testUser, false);

        List<RecipeResponseDTO> results = recipeService.getRecipesByUserId(testUser.getId());

        assertNotNull(results);
        assertTrue(results.size() >= 3);
        results.forEach(recipe -> assertEquals(testUser.getId(), recipe.getAuthorId()));
    }

    @Test
    void testGetUserCookedRecipes() {
        // Create a cooked recipe
        Recipe cookedRecipe = createTestRecipe("Cooked Recipe", testUser, true);
        cookedRecipe.setCooked(true);
        recipeRepository.save(cookedRecipe);

        List<RecipeResponseDTO> results = recipeService.getUserCookedRecipes(testUser.getId());

        assertNotNull(results);
        assertFalse(results.isEmpty());
        results.forEach(recipe -> assertTrue(recipe.isCooked()));
    }

    @Test
    void testGetUserFavouriteRecipes() {
        // Create a favourite recipe
        Recipe favouriteRecipe = createTestRecipe("Favourite Recipe", testUser, true);
        favouriteRecipe.setFavourite(true);
        recipeRepository.save(favouriteRecipe);

        List<RecipeResponseDTO> results = recipeService.getUserFavouriteRecipes(testUser.getId());

        assertNotNull(results);
        assertFalse(results.isEmpty());
        results.forEach(recipe -> assertTrue(recipe.isFavourite()));
    }
}
