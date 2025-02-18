package com.LibXpress.Controller;

import com.LibXpress.Services.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay")
@Tag(name = "Wallet Operations", description = "Operations related to wallet management such as create, deposit, withdraw, and KYC")
public class WellatController {

    private final WalletService walletService;

    public WellatController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "Create a wallet", description = "This endpoint allows a user to create a wallet using their phone number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wallet created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid phone number")
    })
    @PostMapping("/createWallet")
    public ResponseEntity<?> createWallet(@Parameter(description = "Phone number of the user") @RequestParam String phone) {
        if (phone.length() != 10) {
            return new ResponseEntity<>("Invalid phone number", HttpStatus.BAD_REQUEST);
        }
        return walletService.createWallet(phone);
    }

    @Operation(summary = "Deposit amount into wallet", description = "This endpoint allows a user to deposit money into their wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Amount deposited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or negative deposit amount")
    })
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @Parameter(description = "Phone number of the user") @RequestParam String phone,
            @Parameter(description = "Amount to be deposited") @RequestParam double amount) {
        if (amount <= 0) {
            return new ResponseEntity<>("Negative amount can't be deposited", HttpStatus.BAD_REQUEST);
        }
        return walletService.deposit(phone, amount);
    }

    @Operation(summary = "Get wallet balance", description = "This endpoint allows a user to check their wallet balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/getBalance")
    public ResponseEntity<Double> getBalance(@Parameter(description = "Phone number of the user") @RequestParam String phone) {
        return walletService.getBalance(phone);
    }

    @Operation(summary = "Withdraw from wallet", description = "This endpoint allows a user to withdraw money from their wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Amount withdrawn successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or negative withdrawal amount"),
            @ApiResponse(responseCode = "406", description = "Insufficient balance or inactive wallet")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawBalance(
            @Parameter(description = "Phone number of the user") @RequestParam String phone,
            @Parameter(description = "Amount to be withdrawn") @RequestParam Double amount) {
        if (amount <= 0) {
            return new ResponseEntity<>("Negative amount can't be withdrawn", HttpStatus.BAD_REQUEST);
        }
        return walletService.withdrawAmount(phone, amount);
    }

    @Operation(summary = "Complete KYC for wallet", description = "This endpoint allows a user to complete their KYC and activate their wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "KYC completed successfully"),
            @ApiResponse(responseCode = "406", description = "KYC failed or incomplete")
    })
    @GetMapping("/completeKYC")
    public ResponseEntity<?> KYC(@Parameter(description = "Phone number of the user") @RequestParam String phone) {
        boolean isCompleted = walletService.makeKYC(phone);
        if (isCompleted) {
            return new ResponseEntity<>("Your KYC is complete", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Something is missing, please try again", HttpStatus.NOT_ACCEPTABLE);
    }
}
