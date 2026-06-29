package com.inventory.reservation.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import com.inventory.reservation.inventory.entity.InventoryReservation;

public interface ReservationRepository extends JpaRepository<InventoryReservation, Long>{
    
    @Query("SELECT r FROM InventoryReservation WHERE r.orderId= :orderId")
    Optional<InventoryReservation> findByOrderId(Long orderId);
}
