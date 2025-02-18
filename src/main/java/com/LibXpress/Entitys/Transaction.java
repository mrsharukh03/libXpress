package com.LibXpress.Entitys;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
@SequenceGenerator(name = "transaction_seq", sequenceName = "transaction_sequence", initialValue = 1000000000, allocationSize = 1)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email")  // Foreign Key reference to User (Student or Admin)
    private User user; // User can be Student or Admin

    @ManyToOne
    @JoinColumn(name = "book_id")  // Foreign Key reference to Book
    private Books book;

    private LocalDateTime transactionDate;  // The date of the transaction

    private double amount; // The amount of the transaction (if applicable)
    private String payMethod;    // Methods like (UPI, Debit Card , Cash)
    private String transactionStatus; // Status of the transaction (pending, completed, etc.)

    // Constructors, Getters and Setters

}
