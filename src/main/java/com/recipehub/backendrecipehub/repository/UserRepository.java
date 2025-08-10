package com.recipehub.backendrecipehub.repository;

import com.recipehub.backendrecipehub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndDeletedFalse(String username);
    Optional<User> findByEmailAndDeletedFalse(String email);
    Optional<User> findByUsernameAndPassword(String username, String password);
    Optional<User> findByEmailAndPassword(String email, String password);

    // Active-only helpers (exclude soft-deleted users)
    Optional<User> findByIdAndDeletedFalse(Long id);
    List<User> findAllByDeletedFalse();
    
    // Case-insensitive username lookup
    Optional<User> findByUsernameIgnoreCaseAndDeletedFalse(String username);
}