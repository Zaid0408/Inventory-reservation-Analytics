package com.inventory.reservation.inventory.consumer;

import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import lombok.extern.slf4j.Slf4j;
import com.inventory.reservation.inventory.config.QueueConstants;
import com.inventory.reservation.inventory.dto.OrderEvent;
import com.inventory.reservation.inventory.dto.ReservationEvent;
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

    @RabbitListener(queues = QueueConstants.RESERVATION_EXPIRED_QUEUE)
    public void processReservationExpiredEvent(ReservationEvent reservationEvent) {
        try{
            inventoryService.expireReservation(reservationEvent);
        }
        catch (Exception e) {
            log.error("Error expiring reservation for order: {}", reservationEvent, e);
        }
    }
}
