package com.inventory.reservation.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.inventory.reservation.order.dto.CreateOrderRequest;
import com.inventory.reservation.order.dto.CreateOrderResponse;
import com.inventory.reservation.order.service.OrderService;

@RestController
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public CreateOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request)
    {
        return orderService.createOrder(request);
    }
}
