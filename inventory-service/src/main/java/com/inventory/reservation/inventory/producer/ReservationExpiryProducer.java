package com.inventory.reservation.inventory.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.inventory.reservation.inventory.dto.ReservationEvent;
import com.inventory.reservation.inventory.config.QueueConstants;

@Service
@Slf4j
public class ReservationExpiryProducer {
    private final RabbitTemplate rabbitTemplate;

    public ReservationExpiryProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishReservationExpiry(ReservationEvent reservationEvent) {
        rabbitTemplate.convertAndSend(QueueConstants.RESERVATION_TTL_EXCHANGE, QueueConstants.RESERVATION_TTL_ROUTING_KEY, reservationEvent);
        log.info("Reservation expiry event published to RabbitMQ: {}", reservationEvent.getOrderId());
    }
}
