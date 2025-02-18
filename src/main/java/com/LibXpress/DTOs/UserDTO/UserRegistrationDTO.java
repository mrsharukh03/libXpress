package com.LibXpress.DTOs.UserDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class UserRegistrationDTO {

    @Id
    @Email(message = "Please provide a valid email address.")  // Ensures valid email format
    private String email;

    @NotBlank(message = "Name is mandatory.")  // Ensures name is not blank
    private String name;

    @NotBlank(message = "Password is mandatory.")  // Ensures password is not blank
    private String password;

    @NotBlank(message = "Phone number is mandatory.")  // Ensures phone is not blank
    private String phone;

    private LocalDate registrationDate;

    public UserRegistrationDTO(){
        registrationDate = LocalDate.now();
    }
}
