package com.LibXpress.DTOs.BookDTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

    private Long id;
    private String title;
    private String author;
    private String posterURL;
    private List<String> language;
    private String category;
    private int availableQuantity;
    private int totalQuantity;
    private double pricePdf;
    private double priceOffline;
    private boolean isAvailableForBorrow;
    private int borrowDuration;
    private Long views;
}

