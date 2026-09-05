package com.personal.lld.controller;

import com.personal.lld.domain.Product;
import com.personal.lld.service.VendingMachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vending-machines")
@RequiredArgsConstructor
@Slf4j
public class VendingMachineController {

    private final VendingMachineService vendingMachineService;

    @GetMapping("/{machineId}/products")
    public List<Product> getAvailableProducts(
        @PathVariable int machineId
    ) {
        log.info(
            "Controller: Getting available products for machine {}",
            machineId
        );

        return vendingMachineService.getAvailableProducts(machineId);
    }

    @GetMapping("/{machineId}/products/{productId}")
    public Product getProductDetails(
        @PathVariable int machineId,
        @PathVariable int productId
    ) {
        log.info(
            "Controller: Getting product details for product {} in machine {}",
            productId,
            machineId
        );

        return vendingMachineService.getProductDetails(
            machineId,
            productId
        );
    }

    @GetMapping("/{machineId}/inventory")
    public Map<String, Object> getInventoryStatus(
        @PathVariable int machineId
    ) {
        log.info(
            "Controller: Getting inventory status for machine {}",
            machineId
        );

        return vendingMachineService.getInventoryStatus(machineId);
    }

    @GetMapping("/{machineId}/products/{productId}/availability")
    public boolean isProductAvailable(
        @PathVariable int machineId,
        @PathVariable int productId
    ) {
        log.info(
            "Controller: Checking product availability for product {} in machine {}",
            productId,
            machineId
        );

        return vendingMachineService.isProductAvailable(
            machineId,
            productId
        );
    }

    @GetMapping("/{machineId}/products/{productId}/stock")
    public int getProductStock(
        @PathVariable int machineId,
        @PathVariable int productId
    ) {
        log.info(
            "Controller: Getting product stock for product {} in machine {}",
            productId,
            machineId
        );

        return vendingMachineService.getProductStock(
            machineId,
            productId
        );
    }
}
