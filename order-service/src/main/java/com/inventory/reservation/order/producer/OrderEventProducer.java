package com.inventory.reservation.order.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.inventory.reservation.order.dto.OrderEvent;
import com.inventory.reservation.order.config.QueueConstants;

@Service
@Slf4j
public class OrderEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public OrderEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderEvent orderEvent) {
        rabbitTemplate.convertAndSend(QueueConstants.ORDER_EXCHANGE, QueueConstants.ORDER_CREATED_ROUTING_KEY, orderEvent);
        log.info("Order created event published to RabbitMQ: {}", orderEvent.getOrderId());
    }
}
