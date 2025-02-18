package com.LibXpress.APIResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReceipt {
    private Long transactionId;
    private LocalDateTime transactionDate;
    private double bookPrice;
    private double totalAmmout;
    private  int quentity;
    private String payMethod;
    private String bookName;
    private String userName;
    private String userId;
    private String transactionStatus;
}
