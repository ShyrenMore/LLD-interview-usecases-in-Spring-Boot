package com.personal.lld.repository;

import com.personal.lld.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class ProductRepository {

    private final Map<Integer, Product> products = new HashMap<>();
    private int nextProductId = 1;

    public ProductRepository() {
        log.info("ProductRepository initialized");
    }

    public Product findById(int productId) {
        Product product = products.get(productId);

        if (product == null) {
            log.warn("Repository: Product not found with ID: {}", productId);
        } else {
            log.info("Repository: Found product with ID: {}", productId);
        }

        return product;
    }

    public List<Product> findByMachine(int machineId) {
        // For simplicity, return all products since products are not machine-specific.
        List<Product> machineProducts = new ArrayList<>(products.values());

        log.info(
            "Repository: Found {} products for machine {}",
            machineProducts.size(),
            machineId
        );

        return machineProducts;
    }

    public Product save(Product product) {
        if (product.getId() == 0) {
            Product newProduct = new Product(
                nextProductId++,
                product.getName(),
                product.getPrice(),
                product.getCategory()
            );

            products.put(newProduct.getId(), newProduct);
            log.info("Repository: Saved new product with ID: {}", newProduct.getId());

            return newProduct;
        }

        products.put(product.getId(), product);
        log.info("Repository: Updated product with ID: {}", product.getId());

        return product;
    }

    public void delete(int productId) {
        Product removed = products.remove(productId);

        if (removed != null) {
            log.info("Repository: Deleted product with ID: {}", productId);
        } else {
            log.warn("Repository: Product not found for deletion: {}", productId);
        }
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public int getTotalProducts() {
        return products.size();
    }
}
