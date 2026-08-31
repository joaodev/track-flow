package com.joaodev.trackflowapi.order.controller;

import com.joaodev.trackflowapi.order.domain.Order;
import com.joaodev.trackflowapi.order.domain.OrderItem;
import com.joaodev.trackflowapi.order.dto.CreateOrderRequest;
import com.joaodev.trackflowapi.order.dto.ShipOrderRequest;
import com.joaodev.trackflowapi.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        Order created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<Order> list() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @GetMapping("/{id}/items")
    public List<OrderItem> getItems(@PathVariable Long id) {
        return orderService.getItems(id);
    }

    @PatchMapping("/{id}/confirm")
    public Order confirm(@PathVariable Long id) {
        return orderService.confirmOrder(id);
    }

    @PatchMapping("/{id}/ship")
    public Order ship(@PathVariable Long id, @Valid @RequestBody ShipOrderRequest request) {
        return orderService.shipOrder(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public Order cancel(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
