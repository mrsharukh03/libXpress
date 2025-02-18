package com.LibXpress.Entitys;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Wallet {

    @Id
    @Column(name = "phone")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    private double balance;

    private LocalDateTime createdAt;

    private boolean activeStatus;

    // Constructor
    public Wallet() {
        this.createdAt = LocalDateTime.now();
    }


}
