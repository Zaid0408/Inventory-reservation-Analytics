package com.inventory.reservation.inventory.consumer;

import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import lombok.extern.slf4j.Slf4j;
import com.inventory.reservation.inventory.config.QueueConstants;
import com.inventory.reservation.inventory.dto.OrderEvent;

@Component
@Slf4j
public class InventoryConsumer {
    
    @RabbitListener(queues = QueueConstants.ORDER_CREATED_QUEUE)
    public void processOrderEvent(OrderEvent orderEvent) {
        log.info("Received order event: {}", orderEvent);
    }
}
