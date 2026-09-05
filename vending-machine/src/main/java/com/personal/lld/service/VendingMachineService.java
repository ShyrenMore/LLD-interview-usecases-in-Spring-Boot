package com.personal.lld.service;

import com.personal.lld.domain.Product;
import com.personal.lld.domain.ProductCategory;
import com.personal.lld.repository.ProductRepository;
import com.personal.lld.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VendingMachineService {

    private final VendingMachineRepository vendingMachineRepository;
    private final ProductRepository productRepository;

    public List<Product> getAvailableProducts(int machineId) {
        log.info(
            "VendingMachineService: Getting available products for machine {}",
            machineId
        );

        // TODO: Implement actual logic to get products from inventory.
        List<Product> products = new ArrayList<>();

        products.add(
            new Product(1, "Cola", 2.50, ProductCategory.BEVERAGE)
        );
        products.add(
            new Product(2, "Chips", 1.50, ProductCategory.CHIPS)
        );
        products.add(
            new Product(3, "Chocolate", 1.00, ProductCategory.CANDY)
        );
        products.add(
            new Product(4, "Water", 1.00, ProductCategory.BEVERAGE)
        );
        products.add(
            new Product(5, "Cookies", 2.00, ProductCategory.COOKIES)
        );

        log.info(
            "VendingMachineService: Found {} available products",
            products.size()
        );

        return products;
    }

    public Product getProductDetails(
        int machineId,
        int productId
    ) {
        log.info(
            "VendingMachineService: Getting product details for product {} in machine {}",
            productId,
            machineId
        );

        Product product = productRepository.findById(productId);

        if (product == null) {
            product = new Product(
                productId,
                "Product " + productId,
                2.50,
                ProductCategory.BEVERAGE
            );
        }

        log.info(
            "VendingMachineService: Retrieved product: {}",
            product.getName()
        );

        return product;
    }

    public Map<String, Object> getInventoryStatus(int machineId) {
        log.info(
            "VendingMachineService: Getting inventory status for machine {}",
            machineId
        );

        Map<String, Object> status = new HashMap<>();
        status.put("machineId", machineId);
        status.put("totalProducts", 5);
        status.put("lowStockProducts", 1);
        status.put("outOfStockProducts", 0);

        log.info(
            "VendingMachineService: Inventory status retrieved"
        );

        return status;
    }

    public void updateInventory(
        int machineId,
        int productId,
        int quantity
    ) {
        log.info(
            "VendingMachineService: Updating inventory for machine {}, product {}, quantity {}",
            machineId,
            productId,
            quantity
        );

        vendingMachineRepository.updateInventory(
            machineId,
            productId,
            quantity
        );

        log.info(
            "VendingMachineService: Inventory updated successfully"
        );
    }

    public boolean isProductAvailable(
        int machineId,
        int productId
    ) {
        log.info(
            "VendingMachineService: Checking product availability for product {} in machine {}",
            productId,
            machineId
        );

        // TODO: Implement actual availability check.
        return true;
    }

    public int getProductStock(
        int machineId,
        int productId
    ) {
        log.info(
            "VendingMachineService: Getting stock for product {} in machine {}",
            productId,
            machineId
        );

        // TODO: Implement actual stock check.
        return 10;
    }
}
