package com.recipehub.backendrecipehub.repository;

import com.recipehub.backendrecipehub.model.Tag;
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
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    private Tag testTag;
    private Tag testTag2;

    @BeforeEach
    void setUp() {
        // Clean up
        tagRepository.deleteAllInBatch();

        // Create test tags
        testTag = createTestTag("Easy");
        testTag2 = createTestTag("Quick");
    }

    private Tag createTestTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    @Test
    void testFindByNameIgnoreCase_Success() {
        Optional<Tag> result = tagRepository.findByNameIgnoreCase("easy"); // lowercase

        assertTrue(result.isPresent());
        assertEquals("Easy", result.get().getName());
    }

    @Test
    void testFindByNameIgnoreCase_NotFound() {
        Optional<Tag> result = tagRepository.findByNameIgnoreCase("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByNameIgnoreCase_UpperCase() {
        Optional<Tag> result = tagRepository.findByNameIgnoreCase("EASY"); // uppercase

        assertTrue(result.isPresent());
        assertEquals("Easy", result.get().getName());
    }

    @Test
    void testFindByNameIgnoreCase_MixedCase() {
        Optional<Tag> result = tagRepository.findByNameIgnoreCase("EaSy"); // mixed case

        assertTrue(result.isPresent());
        assertEquals("Easy", result.get().getName());
    }

    @Test
    void testFindAll() {
        List<Tag> results = tagRepository.findAll();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(tag -> tag.getName().equals("Easy")));
        assertTrue(results.stream().anyMatch(tag -> tag.getName().equals("Quick")));
    }

    @Test
    void testSave_Success() {
        Tag newTag = new Tag();
        newTag.setName("New Tag");

        Tag savedTag = tagRepository.save(newTag);

        assertNotNull(savedTag);
        assertNotNull(savedTag.getId());
        assertEquals("New Tag", savedTag.getName());
    }

    @Test
    void testSave_Update() {
        testTag.setName("Updated Easy");
        Tag updatedTag = tagRepository.save(testTag);

        assertNotNull(updatedTag);
        assertEquals(testTag.getId(), updatedTag.getId());
        assertEquals("Updated Easy", updatedTag.getName());
    }

    @Test
    void testDelete_Success() {
        Long tagId = testTag.getId();
        tagRepository.delete(testTag);

        Optional<Tag> deletedTag = tagRepository.findById(tagId);
        assertFalse(deletedTag.isPresent());
    }

    @Test
    void testFindById_Success() {
        Optional<Tag> result = tagRepository.findById(testTag.getId());

        assertTrue(result.isPresent());
        assertEquals("Easy", result.get().getName());
    }

    @Test
    void testFindById_NotFound() {
        Optional<Tag> result = tagRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testCount() {
        long count = tagRepository.count();

        assertEquals(2, count);
    }

    @Test
    void testCount_AfterSave() {
        Tag newTag = new Tag();
        newTag.setName("New Tag");
        tagRepository.save(newTag);

        long count = tagRepository.count();

        assertEquals(3, count);
    }

    @Test
    void testCount_AfterDelete() {
        tagRepository.delete(testTag);

        long count = tagRepository.count();

        assertEquals(1, count);
    }
}
