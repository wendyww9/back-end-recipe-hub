package com.recipehub.backendrecipehub.controller;

import com.recipehub.backendrecipehub.service.S3Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
@ConditionalOnBean(S3Client.class)
public class ImageController {

    private final S3Service s3Service;

    public ImageController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "File is empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "File must be an image");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "File size must be less than 5MB");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String fileName = s3Service.uploadImage(file);
            String imageUrl = s3Service.getImageUrl(fileName);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Map<String, String>> getImageUrl(@PathVariable String fileName) {
        try {
            String imageUrl = s3Service.getImageUrl(fileName);
            
            if (imageUrl == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Image not found");
                return ResponseEntity.notFound().build();
            }

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get image URL: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable String fileName) {
        try {
            s3Service.deleteImage(fileName);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Image deleted successfully");
            response.put("fileName", fileName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 