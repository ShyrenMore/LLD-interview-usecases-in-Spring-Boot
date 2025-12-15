package com.personal.lld.controller;

import com.personal.lld.domain.Receipt;
import com.personal.lld.domain.Ticket;
import com.personal.lld.service.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ExitController {
    private final TicketService ticketService;
    private final PricingService pricingService;
    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    private final SlotService slotService;

    public ExitResult exitVehicle(UUID ticketId) {
        System.out.println("[CONTROLLER] Vehicle exit request - Ticket: " + ticketId);

        try {
            // Retrieve ticket
            Optional<Ticket> ticketOpt = ticketService.getTicket(ticketId);
            if (ticketOpt.isEmpty()) {
                return new ExitResult(false, null, 0.0, "Ticket not found");
            }

            Ticket ticket = ticketOpt.get();
            if (!ticket.isActive()) {
                return new ExitResult(false, null, 0.0, "Ticket is not active");
            }

            // Calculate fee
            double fee = pricingService.calculateFee(ticket);
            System.out.println("[CONTROLLER] Fee calculated: " + fee);

            // Process payment with retry
            boolean paymentSuccess = paymentService.processPaymentWithRetry(ticketId, fee, 3);
            if (!paymentSuccess) {
                return new ExitResult(false, null, fee, "Payment failed");
            }

            // Generate receipt
            Receipt receipt = receiptService.generateReceipt(ticket, fee);
            receiptService.markReceiptAsPaid(receipt);

            // Release slot
            slotService.releaseSlot(ticket.getSlotId());

            // Deactivate ticket
            ticketService.deactivateTicket(ticketId);

            System.out.println("[CONTROLLER] Vehicle exit successful - Receipt: " + receipt.getId());
            return new ExitResult(true, receipt.getId(), fee, "Exit successful");

        } catch (Exception e) {
            System.out.println("[CONTROLLER] Vehicle exit failed: " + e.getMessage());
            return new ExitResult(false, null, 0.0, e.getMessage());
        }
    }

    public String generateReceiptText(UUID ticketId) {
        System.out.println("[CONTROLLER] Generating receipt text for ticket: " + ticketId);

        try {
            Optional<Ticket> ticketOpt = ticketService.getTicket(ticketId);
            if (ticketOpt.isEmpty()) {
                return "Ticket not found";
            }

            Ticket ticket = ticketOpt.get();
            double fee = pricingService.calculateFee(ticket);
            Receipt receipt = receiptService.generateReceipt(ticket, fee);

            String receiptText = receiptService.generateReceiptText(receipt, ticket);
            System.out.println("[CONTROLLER] Receipt text generated successfully");
            return receiptText;

        } catch (Exception e) {
            System.out.println("[CONTROLLER] Receipt text generation failed: " + e.getMessage());
            return "Error generating receipt: " + e.getMessage();
        }
    }

    @Data
    @AllArgsConstructor
    public static class ExitResult {
        private final boolean success;
        private final UUID receiptId;
        private final double fee;
        private final String message;
    }
}
