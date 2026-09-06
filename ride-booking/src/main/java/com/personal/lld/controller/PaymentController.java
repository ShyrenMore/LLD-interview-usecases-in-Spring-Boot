package com.personal.lld.controller;

import com.personal.lld.domain.PaymentStatus;
import com.personal.lld.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final RideService rideService;

    @PostMapping("/callback")
    public void handleCallback(
            @RequestParam String transactionId,
            @RequestParam PaymentStatus status) {

        rideService.handlePaymentCallback(
                transactionId,
                status);
    }
}
