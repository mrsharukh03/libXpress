package com.LibXpress.DTOs.TransactionDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private LocalDateTime transactionDate;
    private double amount;
    private Long bookId;
    private String userId;
    private String payMethod;
    private String transactionStatus;
}
