package com.LibXpress.DTOs.BookDTO;

import lombok.Data;

import java.util.List;

@Data
public class BookUpdateDTO {
    private Long bookId;
    private String title;
    private String author;
    private String posterURL;
    private List<String> language;
    private String category;
    private Integer availableQuantity;
    private Integer totalQuantity;
    private Double pricePdf;
    private Double priceOffline;
    private Boolean isAvailableForBorrow;
    private Integer borrowDuration;

    // Getters and setters
}
