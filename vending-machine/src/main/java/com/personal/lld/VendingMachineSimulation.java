package com.personal.lld;

import com.personal.lld.controller.AdminController;
import com.personal.lld.controller.PaymentController;
import com.personal.lld.controller.RecoveryController;
import com.personal.lld.controller.VendingMachineController;
import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.PaymentRequest;
import com.personal.lld.domain.Product;
import com.personal.lld.domain.ProductCategory;
import com.personal.lld.domain.RecoveryStatus;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.VendingMachine;
import com.personal.lld.domain.state.ProcessingPaymentState;
import com.personal.lld.repository.ProductRepository;
import com.personal.lld.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class VendingMachineSimulation implements CommandLineRunner {

    private final VendingMachineRepository vendingMachineRepository;
    private final ProductRepository productRepository;
    private final VendingMachineController vendingMachineController;
    private final PaymentController paymentController;
    private final RecoveryController recoveryController;
    private final AdminController adminController;

    public static void main(String[] args) {
        SpringApplication.run(VendingMachineSimulation.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("=== Vending Machine System Simulation ===");

        // Create a vending machine.
        log.info("--- Creating Vending Machine ---");
        VendingMachine machine = new VendingMachine(1, "Main Lobby");
        vendingMachineRepository.save(machine);

        // Demonstrate system startup recovery.
        log.info("--- System Startup Recovery Check ---");
        recoveryController.checkAndRecoverAtStartup();

        // Add products to the repository.
        log.info("--- Adding Products to Inventory ---");
        Product cola = new Product(
            1,
            "Cola",
            2.50,
            ProductCategory.BEVERAGE
        );
        Product chips = new Product(
            2,
            "Chips",
            1.50,
            ProductCategory.CHIPS
        );
        Product chocolate = new Product(
            3,
            "Chocolate",
            1.00,
            ProductCategory.CANDY
        );
        Product water = new Product(
            4,
            "Water",
            1.00,
            ProductCategory.BEVERAGE
        );
        Product cookies = new Product(
            5,
            "Cookies",
            2.00,
            ProductCategory.COOKIES
        );

        productRepository.save(cola);
        productRepository.save(chips);
        productRepository.save(chocolate);
        productRepository.save(water);
        productRepository.save(cookies);

        machine.addProduct(cola, 10);
        machine.addProduct(chips, 15);
        machine.addProduct(chocolate, 20);
        machine.addProduct(water, 25);
        machine.addProduct(cookies, 12);

        log.info("Added products to inventory");

        // Display available products.
        log.info("--- Available Products ---");
        var products = vendingMachineController.getAvailableProducts(1);

        for (Product product : products) {
            log.info(
                "✓ {} - ${}",
                product.getName(),
                product.getPrice()
            );
        }

        // Process a payment.
        log.info("--- Processing Payment ---");
        Map<Denomination, Integer> payment = new HashMap<>();
        payment.put(Denomination.FIVE_DOLLAR, 1);

        PaymentRequest paymentRequest =
            new PaymentRequest(1, 1, payment);

        Transaction transaction =
            paymentController.processPayment(1, paymentRequest);

        if (transaction != null) {
            log.info("✓ Payment processed successfully");
            log.info("Transaction ID: {}", transaction.getId());
            log.info(
                "Amount Required: ${}",
                transaction.getAmountRequired()
            );
            log.info(
                "Amount Inserted: ${}",
                transaction.getAmountInserted()
            );
            log.info("Status: {}", transaction.getStatus());
        }

        // Display machine state.
        log.info("--- Machine State ---");
        log.info("Current State: {}", machine.getCurrentStateName());
        log.info("Operational: {}", machine.isOperational());

        // Wait for state transitions to complete and show final state.
        log.info("--- State Transition Demonstration ---");
        try {
            Thread.sleep(2000);

            log.info(
                "Final State after payment processing: {}",
                machine.getCurrentStateName()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(
                "State transition demonstration interrupted",
                e
            );
        }

        // Display inventory status.
        log.info("--- Inventory Status ---");
        adminController.displayInventoryStatus(1);

        // Display cash box status.
        log.info("--- Cash Box Status ---");
        double totalCash =
            paymentController.getTotalCashInMachine(1);

        log.info(
            "Total Cash in Machine: ${}",
            totalCash
        );

        Map<Denomination, Integer> cashBox =
            paymentController.getCashBoxStatus(1);

        log.info("Denominations Available:");

        for (Map.Entry<Denomination, Integer> entry : cashBox.entrySet()) {
            log.info(
                "{}: {} notes",
                entry.getKey(),
                entry.getValue()
            );
        }

        // Admin operations.
        log.info("--- Admin Operations ---");
        adminController.displaySystemStatus(1);

        // Restock a product.
        log.info("--- Restocking Product ---");
        adminController.restockProduct(1, 1, 5);

        // Collect cash.
        log.info("--- Collecting Cash ---");
        adminController.collectCash(1);

        // Generate sales report.
        log.info("--- Generating Sales Report ---");
        Date startDate =
            new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date();

        adminController.getSalesReport(
            1,
            startDate,
            endDate
        );

        // Recovery operations.
        log.info("--- Recovery Operations ---");
        RecoveryStatus recoveryStatus =
            recoveryController.getRecoveryStatus(1);

        log.info("Recovery Status: {}", recoveryStatus);

        // Create a recovery entry simulating power failure during payment.
        if (transaction != null) {
            log.info(
                "--- Simulating Power Failure Recovery ---"
            );

            recoveryController.createRecoveryEntry(
                1,
                transaction.getId(),
                new ProcessingPaymentState()
            );

            recoveryController.checkAndRecover(1);
        }

        // Display final inventory.
        log.info("--- Final Inventory Status ---");
        adminController.displayInventoryStatus(1);

        // Display final cash box status.
        log.info("--- Final Cash Box Status ---");
        totalCash = paymentController.getTotalCashInMachine(1);

        log.info(
            "Total Cash in Machine: ${}",
            totalCash
        );

        // Display admin help.
        log.info("--- Admin Help ---");
        log.info(adminController.displayAdminHelp());

        log.info("=== Simulation Complete ===");
        log.info("✓ Vending Machine System - Complete Implementation");
        log.info("  ✓ Domain Layer - All entities and state pattern");
        log.info("  ✓ Repository Layer - Data access abstraction");
        log.info("  ✓ Service Layer - Business logic implementation");
        log.info("  ✓ Controller Layer - API endpoints and user interface");
        log.info("  ✓ State Pattern - Machine state management");
        log.info("  ✓ Payment Processing - Cash handling and change calculation");
        log.info("  ✓ Inventory Management - Product tracking and restocking");
        log.info("  ✓ Recovery System - Power failure handling");
        log.info("  ✓ Admin Operations - Restocking, cash collection, reporting");
    }
}
