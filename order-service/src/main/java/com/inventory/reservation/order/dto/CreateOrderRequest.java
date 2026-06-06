package com.inventory.reservation.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;

public class CreateOrderRequest {
    @NotNull
    @NotEmpty
    private String productId;

    @NotNull
    @Min(1)
    private int quantity;

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
