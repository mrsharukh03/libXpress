package com.LibXpress.Services;

import com.LibXpress.DTOs.UserDTO.AdminRegistrationDTO;
import com.LibXpress.DTOs.UserDTO.UserProfileDTO;
import com.LibXpress.DTOs.UserDTO.UserRegistrationDTO;
import com.LibXpress.Entitys.OTPVerification;
import com.LibXpress.Entitys.User;
import com.LibXpress.JWTCnfig.JwtUtils;
import com.LibXpress.Repositorys.OTPVerificationRepo;
import com.LibXpress.Repositorys.UserRegistationRepo;
import com.LibXpress.Repositorys.UserRepo;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final OTPVerificationRepo otpVerificationRepo;
    private final UserRegistationRepo userRegistationRepo;
    private final MailService mailService;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepo userRepo, OTPVerificationRepo otpVerificationRepo,
                       UserRegistationRepo userRegistationRepo, MailService mailService,
                       ModelMapper modelMapper, JwtUtils jwtUtils) {
        this.userRepo = userRepo;
        this.otpVerificationRepo = otpVerificationRepo;
        this.userRegistationRepo = userRegistationRepo;
        this.mailService = mailService;
        this.modelMapper = modelMapper;
        this.jwtUtils = jwtUtils;
    }

    // Helper method for sending OTP
    public boolean sendOTP(String userId) {
        UserRegistrationDTO userDTO = findRegistationRequest(userId);
        User user = findById(userId);
        if (userDTO == null && user == null) return false;

        OTPVerification verificationData = otpVerificationRepo.findById(userId).orElse(new OTPVerification());

        // Check if OTP count exceeds 10 and handle the 25-hour break logic
        if (verificationData.getOtpCount() >= 10) {
            if (verificationData.getExpiryDate() != null &&
                    verificationData.getExpiryDate().isAfter(LocalDateTime.now().minusHours(25))) {
                return false;
            } else {
                // If 25 hours have passed, reset the OTP count to 0 and set expiryDate to null
                verificationData.setOtpCount(0);
                verificationData.setExpiryDate(LocalDateTime.now());
            }
        }

        // If OTP expiry is still valid (within 1 minute of sending), prevent sending a new OTP
        if (verificationData.getExpiryDate() != null &&
                verificationData.getExpiryDate().isAfter(LocalDateTime.now().minusMinutes(1))) {
            return false;
        }

        // Generate new OTP
        String otp = UserUitlsService.generateOTP();
        verificationData.setEmail(userId);
        verificationData.setOtp(otp);
        verificationData.setExpiryDate(LocalDateTime.now().plusMinutes(2)); // OTP expires in 2 minutes
        verificationData.setOtpCount(verificationData.getOtpCount() + 1);

        try {
            mailService.sendOTPEmail(verificationData);
            otpVerificationRepo.save(verificationData);
            return true;
        } catch (Exception e) {
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
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
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
            userDTO.setRegistrationDate(LocalDate.now());
            userDTO.setPassword(userDTO.getPassword());
            userRegistationRepo.save(userDTO);
            sendOTP(userDTO.getEmail());
            return new ResponseEntity<>(ErrorMessages.OTP_REQUIRED, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
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
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
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


    public ResponseEntity<String> createNewPassword(String userId,String password, String newOTP) {
        User existingUser = findById(userId);
        OTPVerification existingOTP = otpVerificationRepo.findById(userId).orElse(null);
        if(existingOTP == null) return new ResponseEntity<>(ErrorMessages.INVALID_OTP,HttpStatus.NOT_ACCEPTABLE);
        String validationStatus = UserUitlsService.validateUser(existingOTP,newOTP);
        switch (validationStatus) {
            case "Invalid":
                return new ResponseEntity<>(ErrorMessages.INVALID_OTP, HttpStatus.BAD_REQUEST);
            case "expired":
                otpVerificationRepo.deleteById(userId);
                return new ResponseEntity<>(ErrorMessages.OTP_EXPIRED, HttpStatus.BAD_REQUEST);
            case "true":
                if(passwordEncoder.matches(password,existingUser.getPassword())) new ResponseEntity<>("User new password ",HttpStatus.BAD_REQUEST);
                existingUser.setPassword(passwordEncoder.encode(password));
                userRepo.save(existingUser);
                existingOTP.setExpiryDate(LocalDateTime.now().minusMinutes(10));
                otpVerificationRepo.save(existingOTP);
                return new ResponseEntity<>("Your password changed ",HttpStatus.OK);
            default:
                return new ResponseEntity<>(ErrorMessages.UNKNOWN_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> login(String email, String password) {
        try {
            User existingUser = findById(email);
            if (existingUser == null) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            if (passwordEncoder.matches(password, existingUser.getPassword())) {
                String accessToken = jwtUtils.generateToken(existingUser.getEmail());
                String refreshToken = jwtUtils.generateRefreshToken(existingUser.getEmail());
                Map<String,String> loginResponse = new HashMap<>();
                loginResponse.put("Access Token",accessToken);
                loginResponse.put("Refresh Token",refreshToken);
                return new ResponseEntity<>(accessToken, HttpStatus.OK);
            }

            return new ResponseEntity<>("Invalid credentials", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> accessToken(String refreshToken,String username){
        Boolean isValid = jwtUtils.validateToken(refreshToken,username);
        if(!isValid) return new ResponseEntity<>("Invalid Token",HttpStatus.UNAUTHORIZED);
        String accessToken = jwtUtils.generateToken(username);
        return new ResponseEntity<>(accessToken,HttpStatus.OK);
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
