package com.personal.lld.service;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Product;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.VendingMachine;
import com.personal.lld.domain.state.IdleState;
import com.personal.lld.repository.PaymentRepository;
import com.personal.lld.repository.ProductRepository;
import com.personal.lld.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final VendingMachineRepository vendingMachineRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public void restockProduct(int machineId, int productId, int quantity) {
        log.info(
            "AdminService: Restocking product {} with quantity {}",
            productId,
            quantity
        );

        if (!vendingMachineRepository.exists(machineId)) {
            throw new RuntimeException("Machine " + machineId + " not found");
        }

        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product " + productId + " not found");
        }

        VendingMachine machine = vendingMachineRepository.findById(machineId);

        if (machine != null) {
            log.info(
                "AdminService: Current machine state: {}",
                machine.getCurrentStateName()
            );
        }

        vendingMachineRepository.updateInventory(machineId, productId, quantity);

        if (machine != null && !"IDLE".equals(machine.getCurrentStateName())) {
            log.info("AdminService: Resetting machine state to IDLE after restocking");
            machine.setState(new IdleState());
        }

        log.info("AdminService: Product restocked successfully");
    }

    public Map<String, Object> collectCash(int machineId) {
        log.info("AdminService: Collecting cash from machine {}", machineId);

        if (!vendingMachineRepository.exists(machineId)) {
            throw new RuntimeException("Machine " + machineId + " not found");
        }

        Map<Denomination, Integer> cashBox =
            paymentRepository.getCashBox(machineId);

        double totalAmount =
            paymentRepository.getTotalCashInMachine(machineId);

        Map<String, Object> collectionResult = new HashMap<>();
        collectionResult.put("totalAmount", totalAmount);
        collectionResult.put("denominations", cashBox);

        Map<Denomination, Integer> emptyCashBox = new HashMap<>();
        for (Denomination denomination : Denomination.values()) {
            emptyCashBox.put(denomination, 0);
        }

        paymentRepository.updateCashBox(machineId, emptyCashBox);

        log.info(
            "AdminService: Cash collected successfully - Total: ${}",
            totalAmount
        );

        return collectionResult;
    }

    public Map<String, Object> getSalesReport(
        int machineId,
        Date startDate,
        Date endDate
    ) {
        log.info(
            "AdminService: Generating sales report for machine {}",
            machineId
        );

        if (!vendingMachineRepository.exists(machineId)) {
            throw new RuntimeException("Machine " + machineId + " not found");
        }

        List<Transaction> transactions =
            paymentRepository.findByMachine(machineId);

        List<Transaction> filteredTransactions = transactions.stream()
            .filter(transaction -> {
                long timestamp = transaction.getTimestamp();
                return timestamp >= startDate.getTime()
                    && timestamp <= endDate.getTime();
            })
            .toList();

        double totalSales = filteredTransactions.stream()
            .filter(transaction -> transaction.getStatus() == TransactionStatus.COMPLETED)
            .mapToDouble(Transaction::getAmountRequired)
            .sum();

        int totalTransactions = filteredTransactions.size();

        List<String> topProducts =
            List.of("Coca Cola", "Snickers", "Chips", "Water");

        Map<String, Object> report = new HashMap<>();
        report.put("totalSales", totalSales);
        report.put("totalTransactions", totalTransactions);
        report.put("topProducts", topProducts);
        report.put("startDate", startDate);
        report.put("endDate", endDate);

        log.info("AdminService: Sales report generated successfully");

        return report;
    }

    public Map<String, Object> getInventoryStatus(int machineId) {
        log.info(
            "AdminService: Getting inventory status for machine {}",
            machineId
        );

        if (!vendingMachineRepository.exists(machineId)) {
            throw new RuntimeException("Machine " + machineId + " not found");
        }

        List<Product> products =
            productRepository.findByMachine(machineId);

        Map<String, Object> status = new HashMap<>();
        status.put("machineId", machineId);
        status.put("totalProducts", products.size());
        status.put("products", products);

        log.info("AdminService: Inventory status retrieved");

        return status;
    }

    public Map<String, Object> getSystemHealth(int machineId) {
        log.info(
            "AdminService: Checking system health for machine {}",
            machineId
        );

        if (!vendingMachineRepository.exists(machineId)) {
            throw new RuntimeException("Machine " + machineId + " not found");
        }

        VendingMachine machine =
            vendingMachineRepository.findById(machineId);

        boolean machineOperational = machine != null && machine.isOperational();
        int totalProducts = productRepository.getTotalProducts();
        int totalTransactions = paymentRepository.getTotalTransactions();
        double totalCash =
            paymentRepository.getTotalCashInMachine(machineId);

        Map<String, Object> health = new HashMap<>();
        health.put("machineOperational", machineOperational);
        health.put("totalProducts", totalProducts);
        health.put("totalTransactions", totalTransactions);
        health.put("totalCash", totalCash);
        health.put("status", machineOperational ? "HEALTHY" : "UNHEALTHY");

        log.info("AdminService: System health check completed");

        return health;
    }
}
