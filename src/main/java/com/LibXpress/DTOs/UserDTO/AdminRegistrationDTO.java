package com.LibXpress.DTOs.UserDTO;

import lombok.Data;

@Data
public class AdminRegistrationDTO {
    private String email;
    private String name;
    private String password;
    private String phone;
    private String role;
}
