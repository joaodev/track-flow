package com.joaodev.trackflowapi.order.service;

import com.joaodev.trackflowapi.inventory.service.InventoryService;
import com.joaodev.trackflowapi.order.domain.Order;
import com.joaodev.trackflowapi.order.domain.OrderItem;
import com.joaodev.trackflowapi.order.dto.CreateOrderRequest;
import com.joaodev.trackflowapi.order.dto.OrderItemRequest;
import com.joaodev.trackflowapi.order.dto.ShipOrderRequest;
import com.joaodev.trackflowapi.order.event.OrderStatusChangedEvent;
import com.joaodev.trackflowapi.order.repository.OrderItemRepository;
import com.joaodev.trackflowapi.order.repository.OrderRepository;
import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.service.ProductService;
import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.dto.CreateShipmentRequest;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
import com.joaodev.trackflowapi.shipment.service.ShipmentService;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final ShipmentService shipmentService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        ProductService productService, InventoryService inventoryService,
                        ShipmentService shipmentService, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.shipmentService = shipmentService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerName(request.customerName())
                .origin(request.origin())
                .destination(request.destination())
                .status("PENDING")
                .createdAt(now)
                .updatedAt(now)
                .deleted(false)
                .build();

        Order saved = orderRepository.save(order);

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productService.findById(itemRequest.productId());

            if (!product.isActive() || product.isDeleted()) {
                throw new ProductNotOrderableException(product.getId());
            }

            orderItemRepository.save(OrderItem.builder()
                    .orderId(saved.getId())
                    .productId(product.getId())
                    .quantity(itemRequest.quantity())
                    .unitPriceAtOrder(product.getUnitPrice())
                    .build());
        }

        publishStatusChanged(saved, null, "PENDING");
        return saved;
    }

    @Transactional
    public Order confirmOrder(Long id) {
        Order order = findById(id);
        requireStatus(order, "PENDING");

        for (OrderItem item : getItems(order.getId())) {
            inventoryService.reserve(item.getProductId(), item.getQuantity());
        }

        return transitionTo(order, "CONFIRMED");
    }

    @Transactional
    public Order shipOrder(Long id, ShipOrderRequest request) {
        Order order = findById(id);
        requireStatus(order, "CONFIRMED");

        for (OrderItem item : getItems(order.getId())) {
            inventoryService.fullFill(item.getProductId(), item.getQuantity());
        }

        Shipment shipment = shipmentService.createShipment(
                new CreateShipmentRequest(order.getOrigin(), order.getDestination(), request.carrier()));

        order.setShipmentId(shipment.getId());
        return transitionTo(order, "SHIPPED");
    }

    @Transactional
    public Order cancelOrder(Long id) {
        Order order = findById(id);

        switch (order.getStatus()) {
            case "DELIVERED", "CANCELLED" ->
                    throw new InvalidOrderStatusException(order.getOrderNumber(), order.getStatus(), "PENDING, CONFIRMED or SHIPPED");
            case "CONFIRMED" -> {
                for (OrderItem item : getItems(order.getId())) {
                    inventoryService.release(item.getProductId(), item.getQuantity());
                }
            }
            case "SHIPPED" -> {
                Shipment shipment = shipmentService.findById(order.getShipmentId());
                shipmentService.updateStatus(shipment.getTrackingCode(),
                        new UpdateShipmentStatusRequest("CANCELLED", null,
                                "Cancelled with order " + order.getOrderNumber()));
            }
        }

        return transitionTo(order, "CANCELLED");
    }

    @Transactional
    public void markDelivered(Long orderId) {
        Order order = findById(orderId);
        if (!order.getStatus().equals("SHIPPED")) {
            return;
        }
        transitionTo(order, "DELIVERED");
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = findById(id);
        order.setUpdatedAt(LocalDateTime.now());
        order.setDeleted(true);
        orderRepository.save(order);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<Order> findAll() {
        return orderRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    public List<OrderItem> getItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Order findByShipmentId(Long shipmentId) {
        return orderRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new OrderNotFoundException(-1L));
    }

    private void requireStatus(Order order, String required) {
        if (!order.getStatus().equals(required)) {
            throw new InvalidOrderStatusException(order.getOrderNumber(), order.getStatus(), required);
        }
    }

    private Order transitionTo(Order order, String newStatus) {
        String previousStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        publishStatusChanged(saved, previousStatus, newStatus);
        return saved;
    }

    private void publishStatusChanged(Order order, String previousStatus, String newStatus) {
        eventPublisher.publishEvent(new OrderStatusChangedEvent(
                order.getId(), order.getOrderNumber(), previousStatus, newStatus, LocalDateTime.now()));
    }

    private String generateOrderNumber() {
        return "ORD" + UUID.randomUUID().toString().substring(0, 10).toUpperCase().replace("-", "");
    }
}
