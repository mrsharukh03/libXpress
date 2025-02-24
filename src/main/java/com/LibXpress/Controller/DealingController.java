package com.LibXpress.Controller;

import com.LibXpress.DTOs.BookDTO.BookDTO;
import com.LibXpress.DTOs.BookDTO.BorrowBookRequestDTO;
import com.LibXpress.DTOs.BookDTO.FeedbackDTO;
import com.LibXpress.DTOs.UserDTO.UserProfileDTO;
import com.LibXpress.Services.BookService;
import com.LibXpress.Services.DealingService;
import com.LibXpress.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deals")
@Tag(name = "Deal Operations", description = "Operations related to buying books and providing feedback")
public class DealingController {

    @Autowired
    final private BookService bookService;
    final private DealingService dealingService;
    final private UserService userService;

    public DealingController(BookService bookService, DealingService dealingService, UserService userService) {
        this.bookService = bookService;
        this.dealingService = dealingService;
        this.userService = userService;
    }

    @Operation(summary = "Provide Feedback on a Book", description = "This endpoint allows users to provide feedback on a specific book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback saved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/feedback")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Object> feedback(@RequestBody FeedbackDTO feedback,@AuthenticationPrincipal UserDetails userDetails) {
        boolean isFeedbackSaved = bookService.addFeedback(feedback,userDetails.getUsername());
        if (isFeedbackSaved) {
            return new ResponseEntity<>("Thanks for the feedback", HttpStatus.OK);
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(summary = "Buy a Book", description = "This endpoint allows a user to buy a book by providing book ID, user ID, quantity, and payment method.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book purchased successfully"),
            @ApiResponse(responseCode = "404", description = "User or Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    @PostMapping("/buyBook")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> buyBook(
            @Parameter(description = "ID of the book to be bought") @RequestParam @Min(1) Long bookId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Quantity of books to buy") @RequestParam @Min(1) int quantity,
            @Parameter(description = "Payment method chosen for the purchase") @RequestParam @NotBlank String method) {
        UserProfileDTO user = userService.getUserProfile(userDetails.getUsername());
        if (user == null) {
            return new ResponseEntity<>("User not found!", HttpStatus.NOT_FOUND);
        }

        BookDTO existingBook = bookService.getBookDTOById(bookId);
        if (existingBook == null) {
            return new ResponseEntity<>("Book not found!", HttpStatus.NOT_FOUND);
        }
        return dealingService.purchaseBook(bookId, userDetails.getUsername(), quantity, method);
    }

    @GetMapping("/myTransactions")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> userTransaction(@AuthenticationPrincipal UserDetails userDetails){
        return dealingService.findUserTransaction(userDetails.getUsername());
    }

    @PostMapping("/borrowbook")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> borrowBook(@RequestParam BorrowBookRequestDTO borrowRequestDTO,@AuthenticationPrincipal UserDetails userDetails){
        return dealingService.borrowBook(borrowRequestDTO,userDetails.getUsername());
    }

    @PutMapping("/returnBook")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> returnBook(@RequestParam Long borrowedBookId, @AuthenticationPrincipal UserDetails userDetails) {
        return dealingService.returnBook(borrowedBookId, userDetails.getUsername());
    }
}
