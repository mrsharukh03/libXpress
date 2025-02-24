package com.LibXpress.Controller;

import com.LibXpress.DTOs.BookDTO.BookDTO;
import com.LibXpress.DTOs.BookDTO.BookUpdateDTO;
import com.LibXpress.DTOs.UserDTO.AdminRegistrationDTO;
import com.LibXpress.DTOs.UserDTO.UserProfileDTO;
import com.LibXpress.Services.BookService;
import com.LibXpress.Services.TransationService;
import com.LibXpress.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Operations", description = "Operations that can only be performed by an admin, including managing books and users")
@RestController
@RequestMapping("/admin")
public class AdminBookController {

    private final BookService bookService;
    private final UserService userService;
    private final TransationService transationService;

    @Autowired
    public AdminBookController(BookService bookService, UserService userService, TransationService transationService) {
        this.bookService = bookService;
        this.userService = userService;
        this.transationService = transationService;
    }

    @Operation(summary = "Add multiple books", description = "This endpoint allows an admin to add multiple books at once")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Books added successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to add books")
    })
    @PostMapping("/addBooks")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> addBooks(@Validated @RequestBody List<BookDTO> books) {
        boolean isSaved = bookService.saveAll(books);
        if (isSaved) {
            return new ResponseEntity<>("Books added successfully", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Failed to add books", HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Add a single book", description = "This endpoint allows an admin to add a single book to the collection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book added successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to add book")
    })
    @PostMapping("/publishBook")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> addBook(@Validated @RequestBody BookDTO book) {
        boolean isSaved = bookService.save(book);
        if (isSaved) {
            return new ResponseEntity<>("Book added successfully", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Failed to add book", HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Delete a book", description = "This endpoint allows an admin to delete a book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/deleteBook")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteBook(@Parameter(description = "ID of the book to delete") @RequestParam Long id) {
        boolean isDeleted = bookService.deleteById(id);
        if (isDeleted) {
            return new ResponseEntity<>("Book deleted successfully", HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Add a new admin", description = "This endpoint allows the creation of a new admin user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin registered successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to add admin")
    })
    @PostMapping("/addAdmin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> addAdmin(@RequestBody AdminRegistrationDTO adminRegistrationDTO) {
        return userService.save(adminRegistrationDTO);
    }

    @Operation(summary = "Update book details", description = "This endpoint allows an admin to update book details like price, quantity, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid book details or request")
    })
    @PostMapping("/updateBook")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> updateBook(@Validated @RequestBody BookUpdateDTO bookUpdateDTO) {
        boolean isUpdated = bookService.updateBook(bookUpdateDTO);

        if (isUpdated) {
            return new ResponseEntity<>("Book updated successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Please check book details carefully (e.g., bookId, price, quantity)", HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Remove a user", description = "This endpoint allows an admin to remove a user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User removed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/removeUser")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> removeUser(@Parameter(description = "ID of the user to remove") @RequestParam String id) {
        boolean isRemoved = userService.deleteUserById(id);
        if (isRemoved) {
            return new ResponseEntity<>("User Deregistered Successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("User not exist or invalid credits", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get all users", description = "This endpoint allows an admin to retrieve all users' profiles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profiles"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/allUsers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        List<UserProfileDTO> allUsers = userService.getAllUsers();
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    @Operation(summary = "Get all transactions", description = "This endpoint allows an admin to view all transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/allTransistion")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> allTransistions() {
        return transationService.getAllTransistions();
    }
}
