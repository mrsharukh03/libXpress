package com.LibXpress.DTOs.BookDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowBookRequestDTO {
    private Long bookId;
    private int quantity;
    private int durationInDays;
}
