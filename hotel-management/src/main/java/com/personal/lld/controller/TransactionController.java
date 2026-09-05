package com.personal.lld.controller;

import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/bookings/{bookingId}")
    public Transaction initiateTransaction(@PathVariable String bookingId) {
        return transactionService.initiateTransaction(bookingId);
    }

    @PostMapping("/callback")
    public void handleTransactionCallback(
            @RequestParam String providerRef,
            @RequestParam TransactionStatus status) {

        transactionService.handleCallback(providerRef, status);
    }
}
