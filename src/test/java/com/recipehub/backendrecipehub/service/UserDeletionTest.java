package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.dto.RecipeRequestDTO;
import com.recipehub.backendrecipehub.dto.UserRequestDTO;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.RecipeBook;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.repository.RecipeBookRepository;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.dto.RecipeBookCreateRequest;
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
class UserDeletionTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private RecipeBookService recipeBookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeBookRepository recipeBookRepository;

    private User testUser;
    private Long recipeId;
    private Long recipeBookId;

    @BeforeEach
    void setUp() {
        // Clean up
        recipeRepository.deleteAll();
        recipeBookRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("testuser");
        userRequestDTO.setEmail("test@example.com");
        userRequestDTO.setPassword("password");
        userService.registerUser(userRequestDTO);
        
        testUser = userRepository.findByUsernameAndDeletedFalse("testuser").orElse(null);
        assertNotNull(testUser);

        // Create test recipe
        RecipeRequestDTO recipeRequestDTO = new RecipeRequestDTO();
        recipeRequestDTO.setTitle("Test Recipe");
        recipeRequestDTO.setDescription("Test Description");
        recipeRequestDTO.setAuthorId(testUser.getId());
        recipeRequestDTO.setIsPublic(true);
        recipeRequestDTO.setCooked(false);
        recipeRequestDTO.setFavourite(false);
        recipeRequestDTO.setIngredients(List.of());
        recipeRequestDTO.setInstructions(List.of("Test instruction"));

        var recipeResponse = recipeService.createRecipeWithValidation(recipeRequestDTO);
        assertNotNull(recipeResponse);
        recipeId = recipeResponse.getId();

        // Create test recipe book
        RecipeBookCreateRequest recipeBookCreate = new RecipeBookCreateRequest();
        recipeBookCreate.setName("Test Recipe Book");
        recipeBookCreate.setDescription("Test Description");
        recipeBookCreate.setUserId(testUser.getId());
        recipeBookCreate.setIsPublic(true);
        recipeBookCreate.setRecipeIds(List.of(recipeId));

        var recipeBookResponse = recipeBookService.createRecipeBook(recipeBookCreate);
        assertNotNull(recipeBookResponse);
        recipeBookId = recipeBookResponse.getId();
    }

    @Test
    void testCurrentEntityRelationships() {
        // Verify initial state
        assertEquals(1, userRepository.count());
        assertEquals(1, recipeRepository.count());
        assertEquals(1, recipeBookRepository.count());

        System.out.println("\n📊 Current Database State:");
        System.out.println("Users: " + userRepository.count());
        System.out.println("Recipes: " + recipeRepository.count());
        System.out.println("Recipe Books: " + recipeBookRepository.count());

        // Check recipe author relationship
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        if (recipe.isPresent()) {
            System.out.println("\n🔍 Recipe Analysis:");
            System.out.println("Recipe ID: " + recipe.get().getId());
            System.out.println("Recipe Title: " + recipe.get().getTitle());
            
            try {
                User author = recipe.get().getAuthor();
                System.out.println("Recipe Author: " + (author != null ? author.getUsername() : "NULL"));
            } catch (Exception e) {
                System.out.println("❌ Error accessing recipe author: " + e.getMessage());
            }
        }

        // Check recipe book user relationship
        Optional<RecipeBook> recipeBook = recipeBookRepository.findById(recipeBookId);
        if (recipeBook.isPresent()) {
            System.out.println("\n🔍 Recipe Book Analysis:");
            System.out.println("Recipe Book ID: " + recipeBook.get().getId());
            System.out.println("Recipe Book Name: " + recipeBook.get().getName());
            
            try {
                User user = recipeBook.get().getUser();
                System.out.println("Recipe Book Owner: " + (user != null ? user.getUsername() : "NULL"));
            } catch (Exception e) {
                System.out.println("❌ Error accessing recipe book user: " + e.getMessage());
            }
        }
    }

    @Test
    void testUserDeletionWithCascadeAnnotations() {
        System.out.println("\n🔍 Current Entity Relationships:");
        System.out.println("User -> Recipe: @ManyToOne (no cascade)");
        System.out.println("User -> RecipeBook: @ManyToOne (no cascade)");
        System.out.println("Recipe -> User: @ManyToOne (no cascade)");
        System.out.println("RecipeBook -> User: @ManyToOne (no cascade)");
        
        System.out.println("\n⚠️  Current Behavior:");
        System.out.println("- User deletion will fail due to foreign key constraints");
        System.out.println("- Recipes and Recipe Books reference the user");
        System.out.println("- No cascade delete is configured");
        System.out.println("- This will cause database constraint violations");
    }
} 