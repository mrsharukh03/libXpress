package com.LibXpress.Services;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    final private WalletService walletService;

    public PaymentService(WalletService walletService) {
        this.walletService = walletService;
    }

    public boolean payWithUpi(Double ammount) {
        return false;
    }

    public boolean payWithWallet(String phone,Double amount) {
        Double debit = walletService.withdraw(phone,amount);
        if(debit == null) return false;
        return true;
    }
}
