package com.joaodev.trackflowapi.order.event;

import com.joaodev.trackflowapi.order.domain.Order;
import com.joaodev.trackflowapi.order.repository.OrderRepository;
import com.joaodev.trackflowapi.order.service.OrderService;
import com.joaodev.trackflowapi.shipment.event.ShipmentStatusChangedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Component
public class ShipmentDeliveredEventListener {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public ShipmentDeliveredEventListener(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        if (!"DELIVERED".equals(event.newStatus())) {
            return;
        }

        Optional<Order> order = orderRepository.findByShipmentId(event.shipmentId());
        order.ifPresent(o -> orderService.markDelivered(o.getId()));
    }
}
