package com.personal.lld.controller;

import com.personal.lld.domain.AccountStatement;
import com.personal.lld.domain.Wallet;
import com.personal.lld.service.TransactionService;
import com.personal.lld.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;

    @PostMapping
    public Wallet createWallet(@RequestParam String userId) {
        return walletService.createWallet(userId);
    }

    @GetMapping("/{accountNumber}/balance")
    public long getBalance(@PathVariable String accountNumber) {
        return walletService.getByAccountNumber(accountNumber)
                .getBalanceMinor();
    }

    @GetMapping("/{accountNumber}/statement")
    public AccountStatement getStatement(
            @PathVariable String accountNumber,
            @RequestParam(required = false) Long startUtc,
            @RequestParam(required = false) Long endUtc) {

        return transactionService.getStatement(
                accountNumber, startUtc, endUtc);
    }
}
