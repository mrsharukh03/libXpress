package com.LibXpress.Services;

import com.LibXpress.Entitys.User;
import com.LibXpress.Entitys.Wallet;
import com.LibXpress.Repositorys.UserRepo;
import com.LibXpress.Repositorys.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WalletService {

    private final WalletRepo walletRepo;
    private final UserRepo userRepo;

    @Autowired
    public WalletService(WalletRepo walletRepo, UserRepo userRepo) {
        this.walletRepo = walletRepo;
        this.userRepo = userRepo;
    }

    public ResponseEntity<?> createWallet(String phone) {

        // Check if user exists with the provided phone number
        User user = userRepo.findUserByPhone(phone);
        if (user == null) return new ResponseEntity<>("User not found or incorrect phone number", HttpStatus.NOT_FOUND);

        // Check if wallet already exists for this phone number
        Wallet existingWallet = walletRepo.findById(phone).orElse(null);
        if (existingWallet != null) {
            return new ResponseEntity<>("Wallet already exists", HttpStatus.ALREADY_REPORTED);
        }

        // Create and save the new wallet
        Wallet newWallet = new Wallet();
        newWallet.setPhone(phone);
        newWallet.setBalance(0.0);
        newWallet.setCreatedAt(LocalDateTime.now());
        newWallet.setActiveStatus(false);  // If this is intentional
        walletRepo.save(newWallet);

        return new ResponseEntity<>("Wallet created successfully. Please complete KYC to activate the wallet.", HttpStatus.CREATED);
    }

    public ResponseEntity<?> deposit(String phone, double amount) {
        Wallet wallet = walletRepo.findById(phone).orElse(null);
        if(wallet == null) return new ResponseEntity<>("Please Create your wallet",HttpStatus.NOT_FOUND);
        wallet.setBalance(wallet.getBalance()+amount);
        walletRepo.save(wallet);
        return new ResponseEntity<>("Amount "+wallet.getBalance()+" added",HttpStatus.OK);
    }

    public ResponseEntity<Double> getBalance(String phone){
        Wallet wallet = walletRepo.findById(phone).orElse(null);
        if(wallet == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(wallet.getBalance(),HttpStatus.OK);
    }

    public Double withdraw(String phone ,double amount){
        Wallet wallet = walletRepo.findById(phone).orElse(null);
        if (wallet == null) return null;
        if(wallet.getBalance() < amount) return null;
        if(!wallet.isActiveStatus()) return null;
        wallet.setBalance(wallet.getBalance()-amount);
        walletRepo.save(wallet);
        return amount;
    }

    public ResponseEntity<?> withdrawAmount(String phone, Double amount) {
        Wallet wallet = walletRepo.findById(phone).orElse(null);
        if (wallet == null) return new ResponseEntity<>("Wallet Not found",HttpStatus.NOT_FOUND);
        if(!wallet.isActiveStatus()) return new ResponseEntity<>("Please complete your KYC to active your Wallet",HttpStatus.BAD_GATEWAY);
        Double debit = withdraw(phone,amount);
        if(debit == null) return new ResponseEntity<>("Insufficient Balance ",HttpStatus.NOT_ACCEPTABLE);
        return new ResponseEntity<>(debit,HttpStatus.OK);
    }

    public boolean makeKYC(String phone) {
        Wallet wallet = walletRepo.findById(phone).orElse(null);
        if(wallet == null) return false;
        // kyc related Logic
        wallet.setActiveStatus(true);
        walletRepo.save(wallet);
        return true;
    }
}
