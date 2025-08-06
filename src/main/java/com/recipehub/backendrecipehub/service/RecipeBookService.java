package com.recipehub.backendrecipehub.service;

import com.recipehub.backendrecipehub.exception.RecipeNotFoundException;
import com.recipehub.backendrecipehub.exception.RecipeBookNotFoundException;
import com.recipehub.backendrecipehub.exception.UnauthorizedException;
import com.recipehub.backendrecipehub.exception.UserNotFoundException;
import com.recipehub.backendrecipehub.mapper.RecipeMapper;
import org.springframework.stereotype.Service;
import com.recipehub.backendrecipehub.repository.RecipeBookRepository;
import com.recipehub.backendrecipehub.repository.RecipeRepository;
import com.recipehub.backendrecipehub.repository.UserRepository;
import com.recipehub.backendrecipehub.model.RecipeBook;
import com.recipehub.backendrecipehub.model.Recipe;
import com.recipehub.backendrecipehub.model.User;
import com.recipehub.backendrecipehub.dto.RecipeBookDTO;
import com.recipehub.backendrecipehub.dto.UserRequestDTO;
import com.recipehub.backendrecipehub.mapper.RecipeBookMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecipeBookService {

    private final RecipeBookRepository recipeBookRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public RecipeBookService(RecipeBookRepository recipeBookRepository, RecipeRepository recipeRepository, UserRepository userRepository) {
        this.recipeBookRepository = recipeBookRepository;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
    }

    public RecipeBookDTO createRecipeBook(RecipeBookDTO recipeBookDTO) {

        User user = userRepository.findById(recipeBookDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException(recipeBookDTO.getUserId()));

        RecipeBook book = RecipeBookMapper.toEntity(recipeBookDTO, user);
        
        // Handle recipe list if provided
        if (recipeBookDTO.getRecipeIds() != null && !recipeBookDTO.getRecipeIds().isEmpty()) {
            for (Long recipeId : recipeBookDTO.getRecipeIds()) {
                Recipe recipe = recipeRepository.findById(recipeId)
                        .orElseThrow(() -> new RecipeNotFoundException(recipeId));
                book.getRecipes().add(recipe);
            }
        }
        
        RecipeBook savedBook = recipeBookRepository.save(book);

        return RecipeBookMapper.toDTO(savedBook);
    }

    public List<RecipeBookDTO> getUsersAllRecipeBook(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<RecipeBook> recipeBooks = recipeBookRepository.findByUserId(user.getId());

        return RecipeBookMapper.toDTOList(recipeBooks);
    }

    public List<RecipeBookDTO> getAllRecipeBooks() {
        List<RecipeBook> recipeBooks = recipeBookRepository.findAll();
        return RecipeBookMapper.toDTOList(recipeBooks);
    }
    
    public RecipeBookDTO getRecipeBookById(Long id) {
        RecipeBook recipeBook = recipeBookRepository.findById(id)
                .orElseThrow(() -> new RecipeBookNotFoundException(id));

        return RecipeBookMapper.toDTO(recipeBook);
    }

    public RecipeBookDTO updateRecipeBook(Long id, RecipeBookDTO recipeBookDTO) {
        RecipeBook recipeBook = recipeBookRepository.findById(id)
                .orElseThrow(() -> new RecipeBookNotFoundException(id));

        RecipeBookMapper.updateEntity(recipeBookDTO, recipeBook);

        // Handle recipe list updates if provided
        if (recipeBookDTO.getRecipeIds() != null) {
            // Clear current recipes and add new ones
            recipeBook.getRecipes().clear();
            
            for (Long recipeId : recipeBookDTO.getRecipeIds()) {
                Recipe recipe = recipeRepository.findById(recipeId)
                        .orElseThrow(() -> new RecipeNotFoundException(recipeId));
                recipeBook.getRecipes().add(recipe);
            }
        }

        return RecipeBookMapper.toDTO(recipeBookRepository.save(recipeBook));
    }


    public List<RecipeBookDTO> getAllPublicRecipeBooks() {
        List<RecipeBook> recipeBooks = recipeBookRepository.findByIsPublicTrue();
        return RecipeBookMapper.toDTOList(recipeBooks);
    }


    public void deleteRecipeBook(Long id) {
        RecipeBook recipeBook = recipeBookRepository.findById(id)
                .orElseThrow(() -> new RecipeBookNotFoundException(id));

        recipeBookRepository.delete(recipeBook);
    }
}
