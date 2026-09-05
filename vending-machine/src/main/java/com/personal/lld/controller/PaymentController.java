package com.personal.lld.controller;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.PaymentRequest;
import com.personal.lld.domain.Transaction;
import com.personal.lld.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/machines/{machineId}")
    public Transaction processPayment(
        @PathVariable int machineId,
        @RequestBody PaymentRequest request
    ) {
        log.info(
            "Controller: Processing payment for machine {}, product {}",
            machineId,
            request.getProductId()
        );

        return paymentService.processPayment(machineId, request);
    }

    @PostMapping("/machines/{machineId}/transactions/{transactionId}/cancel")
    public void cancelPayment(
        @PathVariable int machineId,
        @PathVariable int transactionId
    ) {
        log.info(
            "Controller: Cancelling payment for machine {}, transaction {}",
            machineId,
            transactionId
        );

        paymentService.cancelPayment(machineId, transactionId);
    }

    @GetMapping("/machines/{machineId}/transactions/{transactionId}/status")
    public String getPaymentStatus(
        @PathVariable int machineId,
        @PathVariable int transactionId
    ) {
        log.info(
            "Controller: Getting payment status for machine {}, transaction {}",
            machineId,
            transactionId
        );

        return paymentService.getPaymentStatus(machineId, transactionId);
    }

    @GetMapping("/machines/{machineId}/transactions")
    public List<Transaction> getTransactionHistory(
        @PathVariable int machineId
    ) {
        log.info(
            "Controller: Getting transaction history for machine {}",
            machineId
        );

        return paymentService.getTransactionHistory(machineId);
    }

    @GetMapping("/machines/{machineId}/cash/total")
    public double getTotalCashInMachine(
        @PathVariable int machineId
    ) {
        log.info(
            "Controller: Getting total cash in machine {}",
            machineId
        );

        return paymentService.getTotalCashInMachine(machineId);
    }

    @GetMapping("/machines/{machineId}/cash")
    public Map<Denomination, Integer> getCashBoxStatus(
        @PathVariable int machineId
    ) {
        log.info(
            "Controller: Getting cash box status for machine {}",
            machineId
        );

        return paymentService.getCashBoxStatus(machineId);
    }
}
