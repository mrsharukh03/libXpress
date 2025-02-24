package com.LibXpress.DTOs.BookDTO;

import lombok.Data;

@Data
public class FeedbackDTO {
   private Long bookID;
   private int rating;
   private String comments;

   public FeedbackDTO(){}


}
