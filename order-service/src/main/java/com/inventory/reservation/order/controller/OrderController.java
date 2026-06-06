package com.inventory.reservation.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.inventory.reservation.order.dto.CreateOrderRequest;
import com.inventory.reservation.order.entity.Order;
import com.inventory.reservation.order.service.OrderService;

public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public Order CreateOrder(@RequestBody CreateOrderRequest request)
    {
        return orderService.createOrder(request);
    }
}
