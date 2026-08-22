package com.joaodev.trackflowapi.shipment.repository;

import com.joaodev.trackflowapi.shipment.domain.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByShipmentIdOrderByOccurredAtAsc(Long shipmentId);
}
