package com.joaodev.trackflowapi.shipment.event;

import java.time.LocalDateTime;

public record ShipmentStatusChangedEvent(
        Long shipmentId,
        String trackingCode,
        String previousStatus,
        String newStatus,
        String location,
        String description,
        LocalDateTime occurredAt
) {
}
