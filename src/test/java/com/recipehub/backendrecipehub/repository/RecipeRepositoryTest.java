package com.recipehub.backendrecipehub.repository;

import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.User;
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
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User testUser2;
    private Recipe testRecipe;
    private Recipe privateRecipe;

    @BeforeEach
    void setUp() {
        // Clean up
        recipeRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // Create test users
        testUser = createTestUser("testuser1", "test1@example.com");
        testUser2 = createTestUser("testuser2", "test2@example.com");

        // Create test recipes
        testRecipe = createTestRecipe("Test Recipe", testUser, true);
        privateRecipe = createTestRecipe("Private Recipe", testUser, false);
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
    void testFindByAuthorIdAndIsPublicTrue_Success() {
        List<Recipe> results = recipeRepository.findByAuthorIdAndIsPublicTrue(testUser.getId());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testRecipe.getTitle(), results.get(0).getTitle());
        assertTrue(results.get(0).isPublic());
        assertEquals(testUser.getId(), results.get(0).getAuthor().getId());
    }

    @Test
    void testFindByAuthorIdAndIsPublicTrue_NoResults() {
        List<Recipe> results = recipeRepository.findByAuthorIdAndIsPublicTrue(testUser2.getId());

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindByAuthorIdAndIsPublicTrue_IncludesPrivateRecipes() {
        // Should not include private recipes
        List<Recipe> results = recipeRepository.findByAuthorIdAndIsPublicTrue(testUser.getId());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isPublic());
        assertNotEquals(privateRecipe.getTitle(), results.get(0).getTitle());
    }

    @Test
    void testFindByAuthorIdAndIsPublicTrue_DeletedUser() {
        // Soft delete the user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        List<Recipe> results = recipeRepository.findByAuthorIdAndIsPublicTrue(testUser.getId());

        // Should still find recipes even if user is deleted
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void testFindByAuthorIdAndIsPublicTrue_DeletedRecipe() {
        // Soft delete the recipe
        testRecipe.setDeleted(true);
        recipeRepository.save(testRecipe);

        List<Recipe> results = recipeRepository.findByAuthorIdAndIsPublicTrue(testUser.getId());

        // Should not find deleted recipes due to @SQLRestriction
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindByIdAndIsPublicTrue_Success() {
        Optional<Recipe> result = recipeRepository.findByIdAndIsPublicTrue(testRecipe.getId());

        assertTrue(result.isPresent());
        assertEquals(testRecipe.getTitle(), result.get().getTitle());
        assertTrue(result.get().isPublic());
    }

    @Test
    void testFindByIdAndIsPublicTrue_PrivateRecipe() {
        Optional<Recipe> result = recipeRepository.findByIdAndIsPublicTrue(privateRecipe.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdAndIsPublicTrue_NotFound() {
        Optional<Recipe> result = recipeRepository.findByIdAndIsPublicTrue(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdAndIsPublicTrue_DeletedRecipe() {
        // Soft delete the recipe
        testRecipe.setDeleted(true);
        recipeRepository.save(testRecipe);

        Optional<Recipe> result = recipeRepository.findByIdAndIsPublicTrue(testRecipe.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIsPublicTrue() {
        // Create additional public recipe
        createTestRecipe("Public Recipe 2", testUser2, true);

        List<Recipe> results = recipeRepository.findByIsPublicTrue();

        assertNotNull(results);
        assertEquals(2, results.size());
        results.forEach(recipe -> assertTrue(recipe.isPublic()));
    }

    @Test
    void testFindByIsPublicTrue_ExcludesPrivateRecipes() {
        List<Recipe> results = recipeRepository.findByIsPublicTrue();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isPublic());
        assertNotEquals(privateRecipe.getTitle(), results.get(0).getTitle());
    }

    @Test
    void testFindByIsPublicTrue_ExcludesDeletedRecipes() {
        // Soft delete the public recipe
        testRecipe.setDeleted(true);
        recipeRepository.save(testRecipe);

        List<Recipe> results = recipeRepository.findByIsPublicTrue();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSoftDeleteBehavior() {
        // Verify recipe exists and is not deleted
        Optional<Recipe> recipeBefore = recipeRepository.findById(testRecipe.getId());
        assertTrue(recipeBefore.isPresent());
        assertFalse(recipeBefore.get().isDeleted());

        // Soft delete the recipe
        testRecipe.setDeleted(true);
        recipeRepository.save(testRecipe);

        // Verify recipe still exists in database but is marked as deleted
        Optional<Recipe> recipeAfter = recipeRepository.findById(testRecipe.getId());
        assertTrue(recipeAfter.isPresent());
        assertTrue(recipeAfter.get().isDeleted());

        // Verify recipe is not returned by public queries
        Optional<Recipe> publicRecipe = recipeRepository.findByIdAndIsPublicTrue(testRecipe.getId());
        assertFalse(publicRecipe.isPresent());
    }

    @Test
    void testFindByAuthorId() {
        // Create additional recipes for the user
        createTestRecipe("User Recipe 1", testUser, true);
        createTestRecipe("User Recipe 2", testUser, false);

        List<Recipe> results = recipeRepository.findByAuthorId(testUser.getId());

        assertNotNull(results);
        assertEquals(4, results.size()); // 3 public + 1 private
        results.forEach(recipe -> assertEquals(testUser.getId(), recipe.getAuthor().getId()));
    }

    @Test
    void testFindByAuthorId_DeletedUser() {
        // Soft delete the user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        List<Recipe> results = recipeRepository.findByAuthorId(testUser.getId());

        // Should still find recipes even if user is deleted
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testFindByAuthorId_DeletedRecipe() {
        // Soft delete one recipe
        testRecipe.setDeleted(true);
        recipeRepository.save(testRecipe);

        List<Recipe> results = recipeRepository.findByAuthorId(testUser.getId());

        // Should not find deleted recipes due to @SQLRestriction
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(privateRecipe.getTitle(), results.get(0).getTitle());
    }

    @Test
    void testFindByAuthorIdAndCookedTrue() {
        // Create a cooked recipe for the user
        Recipe cookedRecipe = createTestRecipe("Cooked Recipe", testUser, true);
        cookedRecipe.setCooked(true);
        recipeRepository.save(cookedRecipe);

        List<Recipe> results = recipeRepository.findByAuthorIdAndCookedTrue(testUser.getId());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isCooked());
        assertEquals(testUser.getId(), results.get(0).getAuthor().getId());
    }

    @Test
    void testFindByAuthorIdAndFavouriteTrue() {
        // Create a favourite recipe for the user
        Recipe favouriteRecipe = createTestRecipe("Favourite Recipe", testUser, true);
        favouriteRecipe.setFavourite(true);
        recipeRepository.save(favouriteRecipe);

        List<Recipe> results = recipeRepository.findByAuthorIdAndFavouriteTrue(testUser.getId());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isFavourite());
        assertEquals(testUser.getId(), results.get(0).getAuthor().getId());
    }

}
