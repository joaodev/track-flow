package com.joaodev.trackflowapi.shipment.event;

import java.time.LocalDateTime;

public record ShipmentDeletedEvent(
        Long shipmentId,
        String trackingCode,
        LocalDateTime deletedAt
) {
}