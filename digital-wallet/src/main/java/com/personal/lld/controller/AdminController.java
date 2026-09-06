package com.personal.lld.controller;

import com.personal.lld.domain.Wallet;
import com.personal.lld.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/wallets")
@RequiredArgsConstructor
public class AdminController {

    private final WalletService walletService;

    @PutMapping("/{accountNumber}/suspend")
    public Wallet suspendWallet(@PathVariable String accountNumber) {
        return walletService.suspendWallet(accountNumber);
    }

    @PutMapping("/{accountNumber}/close")
    public Wallet closeWallet(@PathVariable String accountNumber) {
        return walletService.closeWallet(accountNumber);
    }

    @PutMapping("/{accountNumber}/reopen")
    public Wallet reopenWallet(@PathVariable String accountNumber) {
        return walletService.reopenWallet(accountNumber);
    }
}
