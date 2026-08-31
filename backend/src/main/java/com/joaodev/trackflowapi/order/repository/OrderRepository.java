package com.joaodev.trackflowapi.order.repository;

import com.joaodev.trackflowapi.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByShipmentId(Long shipmentId);
    List<Order> findByDeletedFalseOrderByCreatedAtDesc();
}
