package com.inventory.reservation.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.reservation.inventory.dto.OrderEvent;
import com.inventory.reservation.inventory.dto.ReservationEvent;
import com.inventory.reservation.inventory.repository.ProductRepository;
import com.inventory.reservation.inventory.repository.ReservationRepository;
import com.inventory.reservation.inventory.entity.Product;
import com.inventory.reservation.inventory.entity.InventoryReservation;
import com.inventory.reservation.inventory.producer.ReservationExpiryProducer;
import com.inventory.reservation.inventory.enums.ReservationEnums;
import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class InventoryService {
    
    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationExpiryProducer reservationExpiryProducer;
    private final long reservationTtlMs;

    public InventoryService(ProductRepository productRepository, ReservationRepository reservationRepository, ReservationExpiryProducer reservationExpiryProducer, @Value("${app.reservation.ttl-ms}") long reservationTtlMs) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
        this.reservationExpiryProducer = reservationExpiryProducer;
        this.reservationTtlMs = reservationTtlMs;
    }

    @Transactional
    public void reserveInventory(OrderEvent orderEvent) {
        Product product = productRepository.findProductForUpdate(orderEvent.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));
        // return the exception properly so that client knows that resource not found.
        if(product.getAvailableStock() < orderEvent.getQuantity()){
            throw new RuntimeException("Insufficient inventory");
        }
        // return the exception properly so that client knows that insufficient inventory.

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
        reservation.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(reservationTtlMs)));
        reservationRepository.save(reservation);

        ReservationEvent reservationEvent = ReservationEvent.builder()
            .orderId(orderEvent.getOrderId())
            .build();
        reservationExpiryProducer.publishReservationExpiry(reservationEvent);

    }
}
