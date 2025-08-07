package com.recipehub.backendrecipehub.controller;

import com.recipehub.backendrecipehub.dto.TagDTO;
import com.recipehub.backendrecipehub.model.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.recipehub.backendrecipehub.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> findAllTagsWithRecipeCount() {
        List<TagDTO> tags = tagService.findAllTagsWithRecipeCount();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Tag>> findAllTags() {
        List<Tag> tags = tagService.findAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Tag> getTagByName(@PathVariable String name) {
        try {
            Tag tag = tagService.getTagByName(name);
            return ResponseEntity.ok(tag);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initializePredefinedTags() {
        try {
            tagService.initializePredefinedTags();
            return ResponseEntity.ok("Predefined tags initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error initializing tags: " + e.getMessage());
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<List<TagDTO>> getPopularTags(@RequestParam(defaultValue = "10") int limit) {
        List<TagDTO> popularTags = tagService.getPopularTags(limit);
        return ResponseEntity.ok(popularTags);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<TagDTO>> getCategoryTags() {
        List<TagDTO> categoryTags = tagService.getCategoryTags();
        return ResponseEntity.ok(categoryTags);
    }
}

