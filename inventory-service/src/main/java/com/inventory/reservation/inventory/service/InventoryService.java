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
import com.inventory.reservation.inventory.enums.ReservationEnums.ReservationStatus;

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
        // throw new RuntimeException("DLQ test"); // DLQ test
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

    @Transactional
    public void confirmOrder(OrderEvent orderEvent){
        InventoryReservation reservation=reservationRepository.findByOrderId(orderEvent.getOrderId()).orElseThrow(() -> new RuntimeException("Reservation not found"));
        if(reservation.getStatus() != ReservationStatus.RESERVED){
            throw new RuntimeException("Reservation not reserved for orderId: "+ orderEvent.getOrderId());
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        Product product= productRepository.findProductForUpdate(reservation.getProductId()).orElseThrow(()-> new RuntimeException("Product not found"));
        product.setReservedStock(product.getReservedStock()- reservation.getQuantity()); 
        productRepository.save(product);
    }

    @Transactional
    public void cancelOrder(OrderEvent orderEvent){
        InventoryReservation reservation=reservationRepository.findByOrderId(orderEvent.getOrderId()).orElseThrow(() -> new RuntimeException("Reservation not found"));
        if(reservation.getStatus() != ReservationStatus.RESERVED){
            throw new RuntimeException("Reservation not reserved for orderId: "+ orderEvent.getOrderId());
        }
        reservation.setStatus(ReservationEnums.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        Product product= productRepository.findProductForUpdate(reservation.getProductId()).orElseThrow(()-> new RuntimeException("Product not found"));
        product.setAvailableStock(product.getAvailableStock()+ reservation.getQuantity());
        product.setReservedStock(product.getReservedStock()- reservation.getQuantity()); 
        productRepository.save(product);

        // TODO: Since Product is cancelled , what to do next ?
    }

    @Transactional
    public void expireReservation(ReservationEvent reservationEvent){
        InventoryReservation reservation = reservationRepository.findByOrderId(reservationEvent.getOrderId()).orElseThrow(() -> new RuntimeException("Reservation not found"));
        if(reservation.getStatus() != ReservationEnums.ReservationStatus.RESERVED){
            throw new RuntimeException("Reservation not reserved for orderId: "+ reservationEvent.getOrderId());
        }
        reservation.setStatus(ReservationEnums.ReservationStatus.EXPIRED);
        reservationRepository.save(reservation);
        Product product = productRepository.findProductForUpdate(reservation.getProductId()).orElseThrow(()-> new RuntimeException("Product not found"));
        product.setAvailableStock(product.getAvailableStock() + reservation.getQuantity());
        product.setReservedStock(product.getReservedStock() - reservation.getQuantity()); 
        productRepository.save(product);
    }
}
