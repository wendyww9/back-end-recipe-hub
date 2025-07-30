package com.recipehub.backendrecipehub.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRecipeNotFoundException() {
        RecipeNotFoundException ex = new RecipeNotFoundException(1L);
        ResponseEntity<Map<String, Object>> response = handler.handleRecipeNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Recipe not found", body.get("error"));
        assertEquals("Recipe not found with id: 1", body.get("message"));
        assertEquals(404, body.get("status"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void handleUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException(1L);
        ResponseEntity<Map<String, Object>> response = handler.handleUserNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("User not found", body.get("error"));
        assertEquals("User not found with id: 1", body.get("message"));
        assertEquals(404, body.get("status"));
    }

    @Test
    void handleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Access denied");
        ResponseEntity<Map<String, Object>> response = handler.handleUnauthorizedException(ex);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Unauthorized", body.get("error"));
        assertEquals("Access denied", body.get("message"));
        assertEquals(403, body.get("status"));
    }

    @Test
    void handleDuplicateResourceException() {
        DuplicateResourceException ex = new DuplicateResourceException("Username", "testuser");
        ResponseEntity<Map<String, Object>> response = handler.handleDuplicateResourceException(ex);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Duplicate resource", body.get("error"));
        assertEquals("Username already exists: testuser", body.get("message"));
        assertEquals(409, body.get("status"));
    }

    @Test
    void handleRuntimeException() {
        RuntimeException ex = new RuntimeException("Something went wrong");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Internal server error", body.get("error"));
        assertEquals("Something went wrong", body.get("message"));
        assertEquals(500, body.get("status"));
    }
} 