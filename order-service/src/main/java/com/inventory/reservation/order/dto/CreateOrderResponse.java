package com.inventory.reservation.order.dto;

import lombok.Getter;

@Getter
public class CreateOrderResponse {
    private final int statusCode;
    private final long orderId;
    private final String message;

    public CreateOrderResponse(int statusCode, long orderId, String message) {
        this.statusCode = statusCode;
        this.orderId = orderId;
        this.message = message;
    }
}
