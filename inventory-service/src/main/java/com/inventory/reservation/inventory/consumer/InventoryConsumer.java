package com.inventory.reservation.inventory.consumer;

import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import lombok.extern.slf4j.Slf4j;
import com.inventory.reservation.inventory.config.QueueConstants;
import com.inventory.reservation.inventory.dto.OrderEvent;
import com.inventory.reservation.inventory.service.InventoryService;

@Component
@Slf4j
public class InventoryConsumer {

    private final InventoryService inventoryService;

    public InventoryConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = QueueConstants.ORDER_CREATED_QUEUE)
    public void processOrderEvent(OrderEvent orderEvent) {
        try {
            inventoryService.reserveInventory(orderEvent);
        } catch (Exception e) {
            log.error("Error reserving inventory for order: {}", orderEvent, e);
        }
    }
}
