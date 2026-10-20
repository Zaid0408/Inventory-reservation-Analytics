package com.inventory.reservation.order.service;

import com.inventory.reservation.order.dto.*;
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

    public ConfirmOrderResponse confirmOrder(long orderId)
    {
        Order order= orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.EXPIRED || order.getStatus() == OrderStatus.FAILED)
        {
            return new ConfirmOrderResponse(400, orderId, "Order already confirmed or cancelled or expired or failed, Current status: "+ order.getStatus().getValue());
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        OrderEvent orderEvent= OrderEvent.builder()
            .orderId(order.getId())
            .productId(order.getProductId())
            .quantity(order.getQuantity())
            .build();
        orderEventProducer.publishOrderConfirmed(orderEvent);

        return new ConfirmOrderResponse(200, orderId, "Order confirmed successfully");
    }

    public CancelOrderResponse cancelOrder(long orderId)
    {
        Order order= orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.EXPIRED || order.getStatus() == OrderStatus.FAILED)
        {
            return new CancelOrderResponse(400, orderId, "Order already cancelled or confirmed or expired or failed, Current status: "+ order.getStatus().getValue());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        OrderEvent orderEvent= OrderEvent.builder()
            .orderId(order.getId())
            .productId(order.getProductId())
            .quantity(order.getQuantity())
            .build();
        orderEventProducer.publishOrderCancelled(orderEvent);

        return new CancelOrderResponse(200, orderId, "Order cancelled successfully");
    }
}
