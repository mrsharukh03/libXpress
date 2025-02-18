package com.LibXpress.Entitys;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Books {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Title can't be empty")
    private String title;

    @NotEmpty(message = "Author can't be empty")
    private String author;

    // Uncomment and validate ISBN if required
//    @NotEmpty(message = "ISBN can't be empty")
//    @Pattern(regexp = "^(97[89])?\\d{9}(\\d|X)$", message = "Invalid ISBN format")
//    private String isbn;

    private String posterURL;
    private List<String> language;

    @NotEmpty(message = "Category can't be empty")
    private String category;

    @Positive(message = "Available quantity must be positive")
    private int availableQuantity;

    @PositiveOrZero(message = "Total quantity cannot be negative")
    private int totalQuantity;

    @PositiveOrZero(message = "Price for PDF must be non-negative")
    private double pricePdf;

    @PositiveOrZero(message = "Price for offline must be non-negative")
    private double priceOffline;

    private boolean isAvailableForBorrow;

    @Positive(message = "Borrow duration must be positive")
    private int borrowDuration;  // In days

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Feedback> feedbacks;  // List of feedbacks for this book

    private Long views = 0L;

    // Constructors, getters, and setters

    public Books() {
        // Default constructor
    }
}
