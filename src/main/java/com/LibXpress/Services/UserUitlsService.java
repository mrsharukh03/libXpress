package com.LibXpress.Services;

import com.LibXpress.Entitys.OTPVerification;
import com.LibXpress.Repositorys.UserRepo;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;


@Service
public class UserUitlsService {

    private final UserRepo userRepo;

    public UserUitlsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    // otp genrater
    public static String generateOTP() {
        // Characters allowed for OTP generation
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; //abcdefghijklmnopqrstuvwxyz

        // Creating an instance of SecureRandom for better randomness
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(6);

        // Loop to generate 6 characters OTP
        for (int i = 0; i < 6; i++) {
            // Randomly selecting a character from the allowed characters
            int index = random.nextInt(characters.length());
            otp.append(characters.charAt(index));
        }
        return otp.toString();
    }


    public static String validateUser(OTPVerification otpVerification, String enteredOtp){
        if (otpVerification.getOtp().equals(enteredOtp)) {
            if (otpVerification.getExpiryDate().isAfter(LocalDateTime.now())) {
                return "true";
            } else {
                return "expired";
            }
        } else {
            return "Invalid";
        }
    }


}
