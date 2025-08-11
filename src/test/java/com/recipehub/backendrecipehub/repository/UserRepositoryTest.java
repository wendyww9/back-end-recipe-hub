package com.recipehub.backendrecipehub.repository;

import com.recipehub.backendrecipehub.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String testUsername;
    private String testEmail;

    @BeforeEach
    void setUp() {
        // Clean up
        userRepository.deleteAllInBatch();
        
        // Generate unique test data
        long suffix = System.nanoTime();
        testUsername = "testuser_" + suffix;
        testEmail = "test_" + suffix + "@example.com";
        
        // Create test user
        testUser = createTestUser(testUsername, testEmail);
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encodedpassword");
        user.setDeleted(false);
        return userRepository.save(user);
    }

    @Test
    void testFindByUsernameAndDeletedFalse_Success() {
        Optional<User> result = userRepository.findByUsernameAndDeletedFalse(testUsername);

        assertTrue(result.isPresent());
        assertEquals(testUsername, result.get().getUsername());
        assertEquals(testEmail, result.get().getEmail());
        assertFalse(result.get().isDeleted());
    }

    @Test
    void testFindByUsernameAndDeletedFalse_NotFound() {
        Optional<User> result = userRepository.findByUsernameAndDeletedFalse("nonexistentuser");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUsernameAndDeletedFalse_DeletedUser() {
        // Soft delete the user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        Optional<User> result = userRepository.findByUsernameAndDeletedFalse(testUsername);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmailAndDeletedFalse_Success() {
        Optional<User> result = userRepository.findByEmailAndDeletedFalse(testEmail);

        assertTrue(result.isPresent());
        assertEquals(testUsername, result.get().getUsername());
        assertEquals(testEmail, result.get().getEmail());
        assertFalse(result.get().isDeleted());
    }

    @Test
    void testFindByEmailAndDeletedFalse_NotFound() {
        Optional<User> result = userRepository.findByEmailAndDeletedFalse("nonexistent@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmailAndDeletedFalse_DeletedUser() {
        // Soft delete the user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        Optional<User> result = userRepository.findByEmailAndDeletedFalse(testEmail);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdAndDeletedFalse_Success() {
        Optional<User> result = userRepository.findByIdAndDeletedFalse(testUser.getId());

        assertTrue(result.isPresent());
        assertEquals(testUsername, result.get().getUsername());
        assertEquals(testEmail, result.get().getEmail());
        assertFalse(result.get().isDeleted());
    }

    @Test
    void testFindByIdAndDeletedFalse_NotFound() {
        Optional<User> result = userRepository.findByIdAndDeletedFalse(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdAndDeletedFalse_DeletedUser() {
        // Soft delete the user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        Optional<User> result = userRepository.findByIdAndDeletedFalse(testUser.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllByDeletedFalse() {
        // Create additional users
        createTestUser("user2", "user2@example.com");
        createTestUser("user3", "user3@example.com");

        // Soft delete one user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        var results = userRepository.findAllByDeletedFalse();

        assertEquals(2, results.size());
        results.forEach(user -> assertFalse(user.isDeleted()));
    }

    @Test
    void testFindByUsernameIgnoreCaseAndDeletedFalse_Success() {
        Optional<User> result = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(testUsername.toUpperCase());

        assertTrue(result.isPresent());
        assertEquals(testUsername, result.get().getUsername());
        assertEquals(testEmail, result.get().getEmail());
        assertFalse(result.get().isDeleted());
    }

    @Test
    void testFindByUsernameIgnoreCaseAndDeletedFalse_NotFound() {
        Optional<User> result = userRepository.findByUsernameIgnoreCaseAndDeletedFalse("NONEXISTENTUSER");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUsernameIgnoreCaseAndDeletedFalse_DeletedUser() {
        // Soft delete the user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        Optional<User> result = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(testUsername.toUpperCase());

        assertFalse(result.isPresent());
    }

    @Test
    void testSoftDeleteBehavior() {
        // Verify user exists and is not deleted
        Optional<User> userBefore = userRepository.findById(testUser.getId());
        assertTrue(userBefore.isPresent());
        assertFalse(userBefore.get().isDeleted());

        // Soft delete the user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        // Verify user still exists in database but is marked as deleted
        Optional<User> userAfter = userRepository.findById(testUser.getId());
        assertTrue(userAfter.isPresent());
        assertTrue(userAfter.get().isDeleted());

        // Verify user is not returned by deleted=false queries
        Optional<User> userNotDeleted = userRepository.findByUsernameAndDeletedFalse(testUsername);
        assertFalse(userNotDeleted.isPresent());
    }

    @Test
    void testUsernameUniqueness() {
        // Try to create another user with a different username
        User newUser = new User();
        newUser.setUsername("differentuser");
        newUser.setEmail("different@example.com");
        newUser.setPassword("encodedpassword");
        newUser.setDeleted(false);

        // Should be able to save
        User saved = userRepository.save(newUser);
        assertNotNull(saved.getId());
        assertEquals("differentuser", saved.getUsername());
    }

    @Test
    void testEmailUniqueness() {
        // Try to create another user with a different email
        User newUser = new User();
        newUser.setUsername("differentuser");
        newUser.setEmail("different@example.com");
        newUser.setPassword("encodedpassword");
        newUser.setDeleted(false);

        // Should be able to save
        User saved = userRepository.save(newUser);
        assertNotNull(saved.getId());
        assertEquals("different@example.com", saved.getEmail());
    }
}
