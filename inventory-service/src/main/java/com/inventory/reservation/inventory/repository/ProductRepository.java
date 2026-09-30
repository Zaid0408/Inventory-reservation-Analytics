package com.inventory.reservation.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import com.inventory.reservation.inventory.entity.Product;

import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findProductForUpdate(@Param("id") String id);
}

// Pessimistic Lock is a locking mechanism that prevents multiple transactions from accessing the same resource at the same time.
// You assume collision is very likely. You physically lock the database row the moment you read it, forcing everyone else to wait until you are completely done with your transaction.
// Necessary for inventory management to prevent race conditions and ensure data consistency. This has high DB overhead.
