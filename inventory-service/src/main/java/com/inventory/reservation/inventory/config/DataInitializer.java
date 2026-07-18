package com.inventory.reservation.inventory.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.inventory.reservation.inventory.entity.Product;
import com.inventory.reservation.inventory.repository.ProductRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    public static final String SEED_IPHONE_15_ID = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            Product product = new Product();
            product.setId(SEED_IPHONE_15_ID);
            product.setName("iPhone 15");
            product.setAvailableStock(10);
            product.setReservedStock(0);
            productRepository.save(product);
        }
    }
}
