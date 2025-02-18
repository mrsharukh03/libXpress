package com.LibXpress.Entitys;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "borrowed_books")
public class BorrowedBooks{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email")  // Foreign Key reference to User (Student or Admin)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Books book;

    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;

    // Constructors, Getters, and Setters
    public BorrowedBooks() {
        // Default constructor
    }

}
