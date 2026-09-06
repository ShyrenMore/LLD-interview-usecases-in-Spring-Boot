package com.personal.lld.controller;

import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public Transaction transfer(
            @RequestParam String fromAccountNumber,
            @RequestParam String toAccountNumber,
            @RequestParam long amountMinor,
            @RequestParam(required = false) String description) {

        return transactionService.transfer(
                fromAccountNumber,
                toAccountNumber,
                amountMinor,
                description);
    }

    @PostMapping("/deposit")
    public Transaction initiateDeposit(
            @RequestParam String accountNumber,
            @RequestParam long amountMinor,
            @RequestParam String paymentMethod,
            @RequestParam String paymentGateway,
            @RequestBody Map<String, String> paymentDetails) {

        return transactionService.initiateDeposit(
                accountNumber,
                amountMinor,
                paymentMethod,
                paymentGateway,
                paymentDetails);
    }

    @PostMapping("/deposit/callback")
    public void handlePaymentCallback(
            @RequestParam String providerRef,
            @RequestParam TransactionStatus status) {

        transactionService.handleDepositCallback(providerRef, status);
    }

    @PostMapping("/withdraw")
    public Transaction withdraw(
            @RequestParam String accountNumber,
            @RequestParam long amountMinor,
            @RequestParam(required = false) String description) {

        return transactionService.withdraw(
                accountNumber,
                amountMinor,
                description);
    }
}
