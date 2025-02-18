package com.LibXpress.Entitys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class OTPVerification {
    @Id
    private String email;
    private String otp;
    private LocalDateTime expiryDate;
}
