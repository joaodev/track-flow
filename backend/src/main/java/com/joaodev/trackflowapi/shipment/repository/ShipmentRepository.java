package com.joaodev.trackflowapi.shipment.repository;

import com.joaodev.trackflowapi.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingCodeAndDeletedFalse(String trackingCode);
    List<Shipment> findByDeletedFalseOrderByCreatedAtDesc();
    Optional<Shipment> findByIdAndDeletedFalse(Long id);
}