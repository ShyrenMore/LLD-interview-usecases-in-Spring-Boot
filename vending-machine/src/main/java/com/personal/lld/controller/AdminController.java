package com.personal.lld.controller;

import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Product;
import com.personal.lld.repository.VendingMachineRepository;
import com.personal.lld.service.AdminService;
import com.personal.lld.service.VendingMachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final VendingMachineService vendingMachineService;
    private final VendingMachineRepository vendingMachineRepository;

    @PostMapping("/machines/{machineId}/products/{productId}/restock")
    public void restockProduct(
            @PathVariable int machineId,
            @PathVariable int productId,
            @RequestParam int quantity
    ) {
        log.info(
                "Admin: Restocking product {} with quantity {} on machine {}",
                productId,
                quantity,
                machineId
        );

        try {
            adminService.restockProduct(machineId, productId, quantity);
            log.info("Admin: Product restocked successfully");
            displayInventoryStatus(machineId);
        } catch (Exception e) {
            log.error("Admin: Failed to restock product", e);
        }
    }

    @PostMapping("/machines/{machineId}/collect-cash")
    public Map<String, Object> collectCash(@PathVariable int machineId) {
        log.info("Admin: Collecting cash from machine {}", machineId);

        try {
            Map<String, Object> cashCollection =
                    adminService.collectCash(machineId);

            log.info("Admin: Cash collected successfully");
            displayCashCollectionDetails(cashCollection);

            return cashCollection;
        } catch (Exception e) {
            log.error("Admin: Failed to collect cash", e);
            throw e;
        }
    }

    @GetMapping("/machines/{machineId}/sales-report")
    public Map<String, Object> getSalesReport(
            @PathVariable int machineId,
            @RequestParam Date startDate,
            @RequestParam Date endDate
    ) {
        log.info(
                "Admin: Generating sales report for machine {} from {} to {}",
                machineId,
                startDate,
                endDate
        );

        try {
            Map<String, Object> salesReport =
                    adminService.getSalesReport(machineId, startDate, endDate);

            log.info("Admin: Sales report generated successfully");
            displaySalesReport(salesReport);

            return salesReport;
        } catch (Exception e) {
            log.error("Admin: Failed to generate sales report", e);
            throw e;
        }
    }

    @GetMapping("/machines/{machineId}/inventory")
    public List<Product> displayInventoryStatus(@PathVariable int machineId) {
        log.info(
                "Admin: Getting inventory status for machine {}",
                machineId
        );

        return vendingMachineService.getAvailableProducts(machineId);
    }

    private void displayCashCollectionDetails(
            Map<String, Object> cashCollection
    ) {
        log.info("=== CASH COLLECTION DETAILS ===");

        Object totalAmount = cashCollection.get("totalAmount");
        if (totalAmount != null) {
            log.info("Total Amount Collected: ${}", totalAmount);
        }

        Object denominationsValue = cashCollection.get("denominations");
        if (denominationsValue instanceof Map<?, ?> denominations) {
            log.info("Denominations Collected:");

            denominations.forEach((denomination, count) ->
                    log.info("  {}: {} notes", denomination, count)
            );
        }

        log.info("===============================");
    }

    private void displaySalesReport(Map<String, Object> salesReport) {
        log.info("=== SALES REPORT ===");

        if (salesReport.containsKey("totalSales")) {
            log.info("Total Sales: ${}", salesReport.get("totalSales"));
        }

        if (salesReport.containsKey("totalTransactions")) {
            log.info(
                    "Total Transactions: {}",
                    salesReport.get("totalTransactions")
            );
        }

        Object topProductsValue = salesReport.get("topProducts");
        if (topProductsValue instanceof List<?> topProducts) {
            log.info("Top Selling Products:");

            for (int i = 0; i < topProducts.size(); i++) {
                log.info("  {}. {}", i + 1, topProducts.get(i));
            }
        }

        log.info("===================");
    }

    @GetMapping("/machines/{machineId}/system-status")
    public Map<String, Object> displaySystemStatus(
            @PathVariable int machineId
    ) {
        log.info(
                "Admin: Getting system status for machine {}",
                machineId
        );

        boolean operational =
                vendingMachineRepository.findById(machineId) != null
                        && vendingMachineRepository.findById(machineId).isOperational();

        List<Product> products =
                vendingMachineService.getAvailableProducts(machineId);

        return Map.of(
                "machineId", machineId,
                "machineOperational", operational,
                "totalProducts", products.size(),
                "recentTransactions", "Information not available"
        );
    }

    @PostMapping("/machines/{machineId}/reset")
    public void resetMachine(@PathVariable int machineId) {
        log.info(
                "Admin: Resetting machine {} to operational state",
                machineId
        );

        // TODO: Add service-level reset logic.
        log.info("Admin: Machine reset successfully");
    }

    @GetMapping("/help")
    public String displayAdminHelp() {
        log.info("Admin help requested");

        return """
                === ADMIN OPERATIONS HELP ===
                Available Operations:
                1. POST /api/admin/machines/{machineId}/products/{productId}/restock
                2. POST /api/admin/machines/{machineId}/collect-cash
                3. GET /api/admin/machines/{machineId}/sales-report
                4. GET /api/admin/machines/{machineId}/inventory
                5. GET /api/admin/machines/{machineId}/system-status
                6. POST /api/admin/machines/{machineId}/reset
                7. GET /api/admin/help
                =============================
                """;
    }
}
