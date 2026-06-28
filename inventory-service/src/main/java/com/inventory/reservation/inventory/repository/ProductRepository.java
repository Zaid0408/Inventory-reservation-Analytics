package com.inventory.reservation.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import com.inventory.reservation.inventory.entity.Product;

import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product WHERE p.id= :id")
    Optional<Product> findProductForUpdate(String id);
}
