package com.LibXpress.Services.RecommendationSystem;

import com.LibXpress.DTOs.BookDTO.BookDTO;
import com.LibXpress.Entitys.Books;
import com.LibXpress.Entitys.User;
import com.LibXpress.Repositorys.BookRepo;
import com.LibXpress.Repositorys.UserRepo;
import com.LibXpress.Services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final UserService userService;
    private final UserRepo userRepo;
    private final BookRepo bookRepo;
    private final HybridRecommendation hybridRecommendation;
    private final ModelMapper modelMapper;

    @Autowired
    public RecommendationService(UserService userService, UserRepo userRepo, BookRepo bookRepo, HybridRecommendation hybridRecommendation, ModelMapper modelMapper) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.bookRepo = bookRepo;
        this.hybridRecommendation = hybridRecommendation;
        this.modelMapper = modelMapper;
    }

    public ResponseEntity<?> hybridRecommendation(String username) {
        try {
            User user = userService.findById(username);
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            List<User> allUsers = userRepo.findAll();
            List<Books> allBooks = bookRepo.findAll();

            if (allUsers.isEmpty() || allBooks.isEmpty()) {
                return new ResponseEntity<>("No users or books found in the database", HttpStatus.NOT_FOUND);
            }

            List<Books> recommendedBooks = hybridRecommendation.recommendBooks(user, allUsers, allBooks);

            if (recommendedBooks.isEmpty()) {
                return new ResponseEntity<>("No recommendations found", HttpStatus.NOT_FOUND);
            }

            List<BookDTO> recommendedBookDTOs = recommendedBooks.stream()
                    .map(book -> modelMapper.map(book, BookDTO.class))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(recommendedBookDTOs, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
