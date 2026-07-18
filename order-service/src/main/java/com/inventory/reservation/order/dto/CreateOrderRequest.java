package com.inventory.reservation.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class CreateOrderRequest {
    @NotNull
    @NotEmpty
    @Size(max = 36)
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

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
