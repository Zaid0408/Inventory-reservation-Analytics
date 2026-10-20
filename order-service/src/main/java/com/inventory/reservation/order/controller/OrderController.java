package com.inventory.reservation.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.inventory.reservation.order.dto.*;
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
    @PostMapping("/orders/{orderId}/confirm")
    public ConfirmOrderResponse confirmOrder(@PathVariable long orderId)
    {
        return orderService.confirmOrder(orderId);
    }
    @PostMapping("/orders/{orderId}/cancel")
    public CancelOrderResponse cancelOrder(@PathVariable long orderId)
    {
        return orderService.cancelOrder(orderId);
    }
}
