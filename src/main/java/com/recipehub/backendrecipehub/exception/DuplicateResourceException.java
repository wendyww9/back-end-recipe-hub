package com.recipehub.backendrecipehub.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String resourceType, String value) {
        super(resourceType + " already exists: " + value);
    }
} 