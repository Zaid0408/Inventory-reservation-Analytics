package com.inventory.reservation.order.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    // This is the event that will be published to the rabbitmq queue. Not to be confused with the Order entity.
    private long orderId;
    private String productId;
    private int quantity;
}
