package com.inventory.reservation.inventory.service;

import org.springframework.stereotype.Service;

import com.inventory.reservation.inventory.dto.OrderEvent;
import com.inventory.reservation.inventory.repository.ProductRepository;
import com.inventory.reservation.inventory.repository.ReservationRepository;
import com.inventory.reservation.inventory.entity.Product;
import com.inventory.reservation.inventory.entity.InventoryReservation;
import com.inventory.reservation.inventory.enums.ReservationEnums;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class InventoryService {
    
    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(ProductRepository productRepository, ReservationRepository reservationRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
    }

    public void reserveInventory(OrderEvent orderEvent) {
        Product product = productRepository.findProductForUpdate(orderEvent.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));

        if(product.getAvailableStock() < orderEvent.getQuantity()){
            throw new RuntimeException("Insufficient inventory");
        }

        int availableStock = product.getAvailableStock();
        int reservedStock = product.getReservedStock();

        if(availableStock < orderEvent.getQuantity()){
            throw new RuntimeException("Insufficient inventory");
        }

        product.setAvailableStock(availableStock - orderEvent.getQuantity());
        product.setReservedStock(reservedStock + orderEvent.getQuantity());
        productRepository.save(product);

        InventoryReservation reservation = new InventoryReservation();
        reservation.setOrderId(orderEvent.getOrderId());
        reservation.setProductId(orderEvent.getProductId());
        reservation.setQuantity(orderEvent.getQuantity());
        reservation.setStatus(ReservationEnums.ReservationStatus.RESERVED);
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        reservationRepository.save(reservation);

    }
}
