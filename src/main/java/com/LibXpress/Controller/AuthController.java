package com.LibXpress.Controller;

import com.LibXpress.DTOs.UserDTO.UserRegistrationDTO;
import com.LibXpress.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Authontication Operations", description = "Operations related to user registration and verification")
@RequestMapping("/auth")
public class AuthController {

    final public UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "User Registration", description = "This endpoint allows a user to register with their details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    @PostMapping("/signup")
    public ResponseEntity<String> register(@Validated @RequestBody UserRegistrationDTO userDTO) {
        return userService.registerUser(userDTO);
    }

    @Operation(summary = "Verify User", description = "This endpoint allows a user to verify their account using a 6-digit OTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User verified successfully"),
            @ApiResponse(responseCode = "406", description = "Invalid OTP provided"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/verifyUser")
    public ResponseEntity<String> verifyUser(
            @Parameter(description = "User's ID for verification") @RequestParam @NotNull String email,
            @Parameter(description = "The OTP sent to the user's phone or email") @RequestParam @Size(min = 6, max = 6) String otp) {

        if (otp.length() != 6) {
            return new ResponseEntity<>("Invalid OTP", HttpStatus.NOT_ACCEPTABLE);
        }
        return userService.verifyUser(email, otp);
    }

    @Operation(summary = "Request New OTP", description = "This endpoint allows a user to request a new OTP if the previous one has expired or is invalid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New OTP sent successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/resendOTP")
    public ResponseEntity<String> newOTP(
            @Parameter(description = "User ID for which new OTP needs to be sent") @RequestParam String email) {
        boolean isSent = userService.sendOTP(email);
        if (isSent) {
            return new ResponseEntity<>("OTP sent successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Something went wrong!", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/forgetPassword")
    public ResponseEntity<String> forgetPassword(@RequestParam String email){
        boolean isSent = userService.sendOTP(email);
        if(isSent) return new ResponseEntity<>("OTP Sent Successfully ",HttpStatus.OK);
        return new ResponseEntity<>("Something went wrong! ",HttpStatus.NOT_FOUND);
    }

    @PostMapping("/createNewPassword")
    public ResponseEntity<String> createNewPassword(@RequestParam String email,String password,String newOTP){
        return userService.createNewPassword(email,password,newOTP);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,@RequestParam String password){
        return userService.login(email,password);
    }

    @PatchMapping("/accessToken")
    public ResponseEntity<String> accessToken(@RequestParam String username,@RequestParam String refreshToken){
        return userService.accessToken(refreshToken,username);
    }
}
