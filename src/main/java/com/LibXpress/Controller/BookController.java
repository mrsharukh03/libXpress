package com.LibXpress.Controller;

import com.LibXpress.DTOs.BookDTO.BookDTO;
import com.LibXpress.DTOs.BookDTO.FeedbackDTO;
import com.LibXpress.Services.BookService;
import com.LibXpress.Services.RecommendationSystem.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Book Operations", description = "Operations related to books")
@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private RecommendationService recommendationService;

    // Get all books with pagination
    @Operation(summary = "Get all books", description = "Fetches all books with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books"),
            @ApiResponse(responseCode = "204", description = "No books available"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getAll")
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @Parameter(description = "Page number (default is 0)") @RequestParam(defaultValue = "0") int page,  // Default page number is 0
            @Parameter(description = "Page size (default is 10)") @RequestParam(defaultValue = "10") int size   // Default size is 10
    ) {
        Page<BookDTO> books = bookService.getBooks(page, size);
        if (books.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);  // 204 No Content if no books
        } else {
            return new ResponseEntity<>(books, HttpStatus.OK);
        }
    }

    // Get a book by its ID
    @Operation(summary = "Get a book by ID", description = "Fetches a specific book by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the book"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getBookById")
    public ResponseEntity<?> getBookById(@Parameter(description = "ID of the book to retrieve") @RequestParam @Min(1) Long id) {
        try {
            BookDTO book = bookService.getBookDTOById(id);
            if (book != null && book.getId() != null) {
                bookService.addViews(id); // Increment view count
                return new ResponseEntity<>(book, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Book not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search for books by keyword
    @Operation(summary = "Search for books", description = "Searches for books by their title or description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books"),
            @ApiResponse(responseCode = "204", description = "No books found matching the search criteria"),
            @ApiResponse(responseCode = "400", description = "Invalid search keyword provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(
            @Parameter(description = "Keyword for searching books") @RequestParam String keyword,
            @Parameter(description = "Page number (default is 0)") @RequestParam(defaultValue = "0") int page,   // Default page is 0
            @Parameter(description = "Number of books per page (default is 10)") @RequestParam(defaultValue = "10") int size    // Default size is 10
    ) {
        // Call service layer to handle search with pagination
        List<BookDTO> booksPage = bookService.searchBooks(keyword, page, size);

        if (booksPage.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(booksPage, HttpStatus.OK);
        }
    }

    // Get feedback for a specific book
    @Operation(summary = "Get feedback for a book", description = "Fetches all feedback for a specific book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feedbacks"),
            @ApiResponse(responseCode = "404", description = "No feedback found for this book"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getFeedback")
    public ResponseEntity<List<FeedbackDTO>> getFeedback(@Parameter(description = "ID of the book to get feedback") @RequestParam Long bookId) {
        List<FeedbackDTO> allFeedbacks = bookService.getAllFeedbacks(bookId);
        return new ResponseEntity<>(allFeedbacks, HttpStatus.OK);
    }

    @GetMapping("/recommended")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> recommendedBook(@AuthenticationPrincipal UserDetails userDetails){
        return  recommendationService.hybridRecommendation(userDetails.getUsername());
    }
}
