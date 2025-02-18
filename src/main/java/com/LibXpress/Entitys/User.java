package com.LibXpress.Entitys;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class User {

    @Id
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "User have a name")
    private String name;

    @NotEmpty(message = "Password can't be null or empty")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Column(unique = true, nullable = false)
    private String phone;

    String role = "USER";

    @NotNull(message = "Creation time can't be null")
    LocalDateTime createdAt;

    private boolean activeStatus;

    private LocalDateTime lastLogin;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "wallet_phone", referencedColumnName = "phone")
    private Wallet wallet;

    // Cascade delete feedbacks when user is deleted
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks;

    public User() {
        this.createdAt = LocalDateTime.now();
    }
}
