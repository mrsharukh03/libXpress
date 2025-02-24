package com.LibXpress.Controller;

import com.LibXpress.Services.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay")
@Tag(name = "Wallet Operations", description = "Operations related to wallet management such as create, deposit, withdraw, and KYC")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "Create a wallet", description = "This endpoint allows a user to create a wallet using their phone number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wallet created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid phone number")
    })
    @PostMapping("/createWallet")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createWallet(@AuthenticationPrincipal UserDetails userDetails) {
        return walletService.createWallet(userDetails.getUsername());
    }

    @Operation(summary = "Deposit amount into wallet", description = "This endpoint allows a user to deposit money into their wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Amount deposited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or negative deposit amount")
    })
    @PostMapping("/deposit")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deposit(
            @Parameter(description = "Amount to be deposited") @RequestParam double amount,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (amount <= 0) {
            return new ResponseEntity<>("Negative amount can't be deposited", HttpStatus.BAD_REQUEST);
        }
        return walletService.deposit(userDetails.getUsername(), amount);
    }

    @Operation(summary = "Get wallet balance", description = "This endpoint allows a user to check their wallet balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/getBalance")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        return walletService.getBalance(userId);
    }

    @Operation(summary = "Complete KYC for wallet", description = "This endpoint allows a user to complete their KYC and activate their wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "KYC completed successfully"),
            @ApiResponse(responseCode = "406", description = "KYC failed or incomplete")
    })
    @GetMapping("/completeKYC")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> KYC(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        boolean isCompleted = walletService.makeKYC(userId);
        if (isCompleted) {
            return new ResponseEntity<>("Your KYC is complete", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Something is missing, please try again", HttpStatus.NOT_ACCEPTABLE);
    }
}
