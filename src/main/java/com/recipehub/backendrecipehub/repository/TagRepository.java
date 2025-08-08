package com.recipehub.backendrecipehub.repository;

import com.recipehub.backendrecipehub.dto.TagDTO;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("SELECT new com.recipehub.backendrecipehub.dto.TagDTO(t.id, t.name, SIZE(t.recipes)) FROM Tag t")
    List<TagDTO> findAllWithRecipeCount();

    Optional<Tag> findByNameIgnoreCase(String name);

}
