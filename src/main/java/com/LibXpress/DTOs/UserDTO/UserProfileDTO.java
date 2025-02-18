package com.LibXpress.DTOs.UserDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileDTO {
    private String email;
    private String name;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean activeStatus;

}

