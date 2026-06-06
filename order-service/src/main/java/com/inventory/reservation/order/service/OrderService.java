package com.inventory.reservation.order.service;

import com.inventory.reservation.order.dto.CreateOrderRequest;
import com.inventory.reservation.order.entity.Order;
import com.inventory.reservation.order.enums.OrderEnums.OrderStatus;
import com.inventory.reservation.order.repository.OrderRepository;

import java.time.LocalDateTime;

public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(CreateOrderRequest request)
    {
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        return order;
    }
}
