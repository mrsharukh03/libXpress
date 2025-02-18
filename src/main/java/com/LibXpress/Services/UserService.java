package com.LibXpress.Services;

import com.LibXpress.DTOs.UserDTO.AdminRegistrationDTO;
import com.LibXpress.DTOs.UserDTO.UserProfileDTO;
import com.LibXpress.DTOs.UserDTO.UserRegistrationDTO;
import com.LibXpress.Entitys.OTPVerification;
import com.LibXpress.Entitys.User;
import com.LibXpress.Repositorys.OTPVerificationRepo;
import com.LibXpress.Repositorys.UserRegistationRepo;
import com.LibXpress.Repositorys.UserRepo;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final OTPVerificationRepo otpVerificationRepo;
    private final UserRegistationRepo userRegistationRepo;
    private final MailService mailService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserService(UserRepo userRepo, OTPVerificationRepo otpVerificationRepo,
                       UserRegistationRepo userRegistationRepo, MailService mailService,
                       ModelMapper modelMapper) {
        this.userRepo = userRepo;
        this.otpVerificationRepo = otpVerificationRepo;
        this.userRegistationRepo = userRegistationRepo;
        this.mailService = mailService;
        this.modelMapper = modelMapper;
    }

    // Helper method for sending OTP
    @Async
    public boolean sendOTP(String userId) {
        User user = findById(userId);
        if (user == null) return false;

        String otp = UserUitlsService.generateOTP();
        OTPVerification verificationData = new OTPVerification();
        verificationData.setEmail(userId);
        verificationData.setOtp(otp);
        verificationData.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // 10 minutes expiry time

        try {
            otpVerificationRepo.save(verificationData);
            mailService.sendOTPEmail(verificationData);
            return true;
        } catch (Exception e) {
            // Log the exception for better debugging
            e.printStackTrace();
            return false;
        }
    }

    // Verify user and handle OTP expiration and validation
    @Transactional
    public ResponseEntity<String> verifyUser(String userId, String otp) {
        UserRegistrationDTO userDTO = findRegistationRequest(userId);
        if (userDTO == null) return new ResponseEntity<>(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);

        OTPVerification existingData = otpVerificationRepo.findById(userId).orElse(null);
        if (existingData == null) return new ResponseEntity<>(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);

        String validationStatus = UserUitlsService.validateUser(existingData, otp);

        switch (validationStatus) {
            case "Invalid":
                return new ResponseEntity<>(ErrorMessages.INVALID_OTP, HttpStatus.BAD_REQUEST);
            case "expired":
                otpVerificationRepo.deleteById(userId);
                return new ResponseEntity<>(ErrorMessages.OTP_EXPIRED, HttpStatus.BAD_REQUEST);
            case "true":
                return handleValidOTP(userDTO, userId);
            default:
                return new ResponseEntity<>(ErrorMessages.UNKNOWN_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to handle successful OTP verification
    private ResponseEntity<String> handleValidOTP(UserRegistrationDTO userDTO, String userId) {
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setName(userDTO.getName());
        user.setPhone(userDTO.getPhone());
        user.setRole("USER");
        user.setActiveStatus(true);
        userRepo.save(user);

        otpVerificationRepo.deleteById(userId);
        userRegistationRepo.deleteById(userId);

        return new ResponseEntity<>("User Registration Success, now you can log in.", HttpStatus.OK);
    }

    public ResponseEntity<String> registerUser(UserRegistrationDTO userDTO) {
        try {
            if (userExists(userDTO.getEmail())) {
                return new ResponseEntity<>(ErrorMessages.USER_ALREADY_EXISTS, HttpStatus.ALREADY_REPORTED);
            }
            if (findRegistationRequest(userDTO.getEmail()) != null) {
                sendOTP(userDTO.getEmail());
                return new ResponseEntity<>(ErrorMessages.OTP_SENT, HttpStatus.ALREADY_REPORTED);
            }

            userRegistationRepo.save(userDTO);
            sendOTP(userDTO.getEmail());
            return new ResponseEntity<>(ErrorMessages.OTP_REQUIRED, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return new ResponseEntity<>(ErrorMessages.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> save(AdminRegistrationDTO userDTO) {
        try {
            if (userExists(userDTO.getEmail())) {
                return new ResponseEntity<>(ErrorMessages.USER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }

            User user = new User();
            user.setEmail(userDTO.getEmail());
            user.setPassword(userDTO.getPassword());
            user.setName(userDTO.getName());
            user.setPhone(userDTO.getPhone());
            user.setRole(userDTO.getRole());
            user.setActiveStatus(true);
            userRepo.save(user);

            return new ResponseEntity<>("Admin User Registered Successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return new ResponseEntity<>(ErrorMessages.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean userExists(String email) {
        return findById(email) != null;
    }

    public User findById(String email) {
        return userRepo.findById(email).orElse(null);
    }

    public UserRegistrationDTO findRegistationRequest(String userId) {
        return userRegistationRepo.findById(userId).orElse(null);
    }

    public boolean deleteUserById(String email) {
        User user = findById(email);
        if (user != null) {
            userRepo.deleteById(email);
            return true;
        }
        return false;
    }

    public List<UserProfileDTO> getAllUsers() {
        List<User> allUsers = userRepo.findAll();
        return allUsers.isEmpty() ? new ArrayList<>() : allUsers.stream()
                .map(user -> modelMapper.map(user, UserProfileDTO.class))
                .collect(Collectors.toList());
    }

    public UserProfileDTO getUserProfile(String userId) {
        User existingUser = findById(userId);
        return existingUser == null ? new UserProfileDTO() : modelMapper.map(existingUser, UserProfileDTO.class);
    }

}

class ErrorMessages {
    public static final String USER_NOT_FOUND = "User not found!";
    public static final String INVALID_OTP = "Invalid OTP";
    public static final String OTP_EXPIRED = "OTP expired";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String OTP_SENT = "Please verify your email OTP sent to your email";
    public static final String OTP_REQUIRED = "Please enter OTP to verify yourself! OTP sent to your registered email";
    public static final String INTERNAL_ERROR = "Something went wrong";
    public static final String UNKNOWN_ERROR = "Unknown error occurred";
}
