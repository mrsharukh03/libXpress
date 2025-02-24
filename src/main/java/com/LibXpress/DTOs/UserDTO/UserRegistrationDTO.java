package com.LibXpress.DTOs.UserDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class UserRegistrationDTO {

    @Id
    @Email(message = "Please provide a valid email address.")
    private String email;

    @NotBlank(message = "Name is mandatory.")
    private String name;

    @NotBlank(message = "Password is mandatory.")
    private String password;

    @NotBlank(message = "Phone number is mandatory.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    private LocalDate registrationDate;
}
