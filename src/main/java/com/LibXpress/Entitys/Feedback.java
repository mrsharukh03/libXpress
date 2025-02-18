package com.LibXpress.Entitys;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    private Books book;

    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "email")
    private User user;

    @Positive(message = "Rating must be positive and between 1 to 5")
    private int rating;

    private String comments;
    
    private LocalDateTime date;

    // Constructors, getters, and setters

    public Feedback() {
        this.date = LocalDateTime.now();
    }
}
