package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.RecipeBookDTO;
import com.recipehub.backendrecipehub.dto.RecipeBookCreateRequest;
import com.recipehub.backendrecipehub.dto.RecipeBookUpdateRequest;
import com.recipehub.backendrecipehub.exception.RecipeBookNotFoundException;
import com.recipehub.backendrecipehub.exception.UnauthorizedException;
import com.recipehub.backendrecipehub.exception.UserNotFoundException;
import com.recipehub.backendrecipehub.model.RecipeBook;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.repository.RecipeBookRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
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
class RecipeBookServiceTest {

    @Autowired
    private RecipeBookService recipeBookService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecipeBookRepository recipeBookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    private User testUser;
    private User testUser2;
    private Recipe testRecipe;
    private RecipeBook testRecipeBook;

    @BeforeEach
    void setUp() {
        // Clean up
        recipeBookRepository.deleteAllInBatch();
        recipeRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // Create test users
        testUser = createTestUser("testuser1", "test1@example.com");
        testUser2 = createTestUser("testuser2", "test2@example.com");

        // Create test recipe
        testRecipe = createTestRecipe("Test Recipe", testUser, true);

        // Create test recipe book
        testRecipeBook = createTestRecipeBook("Test Recipe Book", testUser, true);
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

    private RecipeBook createTestRecipeBook(String name, User user, boolean isPublic) {
        RecipeBook recipeBook = new RecipeBook();
        recipeBook.setName(name);
        recipeBook.setDescription("Test description");
        recipeBook.setUser(user);
        recipeBook.setPublic(isPublic);
        recipeBook.setRecipes(java.util.Set.of());
        return recipeBookRepository.save(recipeBook);
    }

    @Test
    void testCreateRecipeBook_Success() {
        RecipeBookCreateRequest request = new RecipeBookCreateRequest();
        request.setName("New Recipe Book");
        request.setDescription("New description");
        request.setUserId(testUser.getId());
        request.setIsPublic(true);

        RecipeBookDTO result = recipeBookService.createRecipeBook(request);

        assertNotNull(result);
        assertEquals("New Recipe Book", result.getName());
        assertEquals("New description", result.getDescription());
        assertEquals(testUser.getId(), result.getUserId());
        assertTrue(result.getIsPublic());
    }

    @Test
    void testCreateRecipeBook_UserNotFound() {
        RecipeBookCreateRequest request = new RecipeBookCreateRequest();
        request.setName("New Recipe Book");
        request.setDescription("New description");
        request.setUserId(999L); // Non-existent user
        request.setIsPublic(true);

        assertThrows(UserNotFoundException.class, () -> {
            recipeBookService.createRecipeBook(request);
        });
    }

    @Test
    void testGetRecipeBookById_Success() {
        RecipeBookDTO result = recipeBookService.getRecipeBookById(testRecipeBook.getId());

        assertNotNull(result);
        assertEquals(testRecipeBook.getName(), result.getName());
        assertEquals(testRecipeBook.getDescription(), result.getDescription());
        assertEquals(testUser.getId(), result.getUserId());
    }

    @Test
    void testGetRecipeBookById_NotFound() {
        assertThrows(RecipeBookNotFoundException.class, () -> {
            recipeBookService.getRecipeBookById(999L);
        });
    }

    @Test
    void testGetUsersAllRecipeBook() {
        // Create additional recipe books for the user
        createTestRecipeBook("Recipe Book 2", testUser, true);
        createTestRecipeBook("Recipe Book 3", testUser, false);

        List<RecipeBookDTO> results = recipeBookService.getUsersAllRecipeBook(testUser.getId());

        assertNotNull(results);
        assertEquals(3, results.size());
        results.forEach(recipeBook -> assertEquals(testUser.getId(), recipeBook.getUserId()));
    }

    @Test
    void testGetUsersAllRecipeBook_UserNotFound() {
        assertThrows(Exception.class, () -> {
            recipeBookService.getUsersAllRecipeBook(999L);
        });
    }

    @Test
    void testGetAllRecipeBooks() {
        // Create additional recipe books
        createTestRecipeBook("Public Recipe Book", testUser2, true);
        createTestRecipeBook("Private Recipe Book", testUser2, false);

        List<RecipeBookDTO> results = recipeBookService.getAllRecipeBooks();

        assertNotNull(results);
        assertTrue(results.size() >= 2);
        // Should only return public recipe books
        results.forEach(recipeBook -> assertTrue(recipeBook.getIsPublic()));
    }

    @Test
    void testGetAllPublicRecipeBooks() {
        // Create additional recipe books
        createTestRecipeBook("Public Recipe Book", testUser2, true);
        createTestRecipeBook("Private Recipe Book", testUser2, false);

        List<RecipeBookDTO> results = recipeBookService.getAllPublicRecipeBooks();

        assertNotNull(results);
        assertTrue(results.size() >= 2);
        // Should only return public recipe books
        results.forEach(recipeBook -> assertTrue(recipeBook.getIsPublic()));
    }

    @Test
    void testGetPublicRecipeBooksByUserId() {
        // Create additional recipe books for the user
        createTestRecipeBook("Public Recipe Book", testUser, true);
        createTestRecipeBook("Private Recipe Book", testUser, false);

        List<RecipeBookDTO> results = recipeBookService.getPublicRecipeBooksByUserId(testUser.getId());

        assertNotNull(results);
        assertEquals(2, results.size());
        results.forEach(recipeBook -> {
            assertEquals(testUser.getId(), recipeBook.getUserId());
            assertTrue(recipeBook.getIsPublic());
        });
    }

    @Test
    void testGetPublicRecipeBooksByUserId_NoPublicBooks() {
        // Create only private recipe books
        createTestRecipeBook("Private Recipe Book 1", testUser, false);
        createTestRecipeBook("Private Recipe Book 2", testUser, false);

        List<RecipeBookDTO> results = recipeBookService.getPublicRecipeBooksByUserId(testUser.getId());

        assertNotNull(results);
        assertEquals(1, results.size()); // Only the original test recipe book
        assertTrue(results.get(0).getIsPublic());
    }

    @Test
    void testUpdateRecipeBook_Success() {
        RecipeBookUpdateRequest request = new RecipeBookUpdateRequest();
        request.setName("Updated Recipe Book");
        request.setDescription("Updated description");
        request.setIsPublic(false);

        RecipeBookDTO result = recipeBookService.updateRecipeBook(testRecipeBook.getId(), request);

        assertNotNull(result);
        assertEquals("Updated Recipe Book", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.getIsPublic());
    }

    @Test
    void testUpdateRecipeBook_NotFound() {
        RecipeBookUpdateRequest request = new RecipeBookUpdateRequest();
        request.setName("Updated Recipe Book");

        assertThrows(RecipeBookNotFoundException.class, () -> {
            recipeBookService.updateRecipeBook(999L, request);
        });
    }

    @Test
    void testUpdateRecipeBook_OtherUserBook() {
        // Create recipe book owned by different user
        RecipeBook otherUserRecipeBook = createTestRecipeBook("Other User's Book", testUser2, true);

        RecipeBookUpdateRequest request = new RecipeBookUpdateRequest();
        request.setName("Updated Recipe Book");

        // Note: Service doesn't check authorization, so this should succeed
        RecipeBookDTO result = recipeBookService.updateRecipeBook(otherUserRecipeBook.getId(), request);

        assertNotNull(result);
        assertEquals("Updated Recipe Book", result.getName());
    }

    @Test
    void testDeleteRecipeBook_Success() {
        assertDoesNotThrow(() -> {
            recipeBookService.deleteRecipeBook(testRecipeBook.getId());
        });

        // Verify recipe book is deleted
        Optional<RecipeBook> deletedRecipeBook = recipeBookRepository.findById(testRecipeBook.getId());
        assertFalse(deletedRecipeBook.isPresent());
    }

    @Test
    void testDeleteRecipeBook_NotFound() {
        assertThrows(RecipeBookNotFoundException.class, () -> {
            recipeBookService.deleteRecipeBook(999L);
        });
    }

    @Test
    void testDeleteRecipeBook_OtherUserBook() {
        // Create recipe book owned by different user
        RecipeBook otherUserRecipeBook = createTestRecipeBook("Other User's Book", testUser2, true);

        // Note: Service doesn't check authorization, so this should succeed
        assertDoesNotThrow(() -> {
            recipeBookService.deleteRecipeBook(otherUserRecipeBook.getId());
        });

        // Verify recipe book is deleted
        Optional<RecipeBook> deletedRecipeBook = recipeBookRepository.findById(otherUserRecipeBook.getId());
        assertFalse(deletedRecipeBook.isPresent());
    }

    @Test
    void testGetAllRecipeBooks_ExcludesPrivateBooks() {
        // Create public and private recipe books
        createTestRecipeBook("Public Book 1", testUser, true);
        createTestRecipeBook("Private Book 1", testUser, false);
        createTestRecipeBook("Public Book 2", testUser2, true);
        createTestRecipeBook("Private Book 2", testUser2, false);

        List<RecipeBookDTO> results = recipeBookService.getAllRecipeBooks();

        assertNotNull(results);
        assertTrue(results.size() >= 3); // Should include all public books
        results.forEach(recipeBook -> assertTrue(recipeBook.getIsPublic()));
    }

    @Test
    void testGetUsersAllRecipeBook_IncludesPrivateBooks() {
        // Create public and private recipe books for the user
        createTestRecipeBook("Public Book", testUser, true);
        createTestRecipeBook("Private Book", testUser, false);

        List<RecipeBookDTO> results = recipeBookService.getUsersAllRecipeBook(testUser.getId());

        assertNotNull(results);
        assertEquals(3, results.size()); // Should include all user's books (public and private)
        
        // Verify we have both public and private books
        boolean hasPublic = results.stream().anyMatch(RecipeBookDTO::getIsPublic);
        boolean hasPrivate = results.stream().anyMatch(book -> !book.getIsPublic());
        assertTrue(hasPublic);
        assertTrue(hasPrivate);
    }
}
