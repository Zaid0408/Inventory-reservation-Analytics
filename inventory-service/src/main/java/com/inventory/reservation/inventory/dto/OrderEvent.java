package com.inventory.reservation.inventory.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
// This is the event that will be published to the rabbitmq queue and received by the inventory service. Same as Order Event in order-service but with different purpose.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private long orderId;
    private String productId;
    private int quantity;
}
