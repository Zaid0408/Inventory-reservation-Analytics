package com.inventory.reservation.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Column;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {
    
    @Id
    @Column(name = "id",unique = true)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "available_stock")
    private int availableStock;

    @Column(name = "reserved_stock")
    private int reservedStock=0;
}
