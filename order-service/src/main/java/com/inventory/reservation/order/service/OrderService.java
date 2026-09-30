package com.inventory.reservation.order.service;

import com.inventory.reservation.order.dto.CreateOrderRequest;
import com.inventory.reservation.order.dto.CreateOrderResponse;
import com.inventory.reservation.order.dto.OrderEvent;
import com.inventory.reservation.order.entity.Order;
import com.inventory.reservation.order.enums.OrderEnums.OrderStatus;
import com.inventory.reservation.order.repository.OrderRepository;
import com.inventory.reservation.order.producer.OrderEventProducer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer= orderEventProducer;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request)
    {
        Order order = new Order(); // this is entity used to save to database
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        OrderEvent orderEvent=OrderEvent.builder() // this is event used to send to rabbitmq build from the entity order
            .orderId(order.getId())
            .productId(order.getProductId())
            .quantity(order.getQuantity())
            .build();
        // implement try catch here 
        orderEventProducer.publishOrderCreated(orderEvent); // Will publish the event to rabbit MQ

        // Later enhancement is to keep the queue in asynchronous mode so in the background it will do its job of publishing but we early return the response to the client.
        return new CreateOrderResponse(201, order.getId(), "Order created successfully");
    }
}
