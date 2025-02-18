package com.LibXpress.DTOs.BookDTO;

import lombok.Data;

@Data
public class FeedbackDTO {
   private Long bookID;
   private String userEmail;
   private int rating;
   private String comments;

   public FeedbackDTO(){}


}
