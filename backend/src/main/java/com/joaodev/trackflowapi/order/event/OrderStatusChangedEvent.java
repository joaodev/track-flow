package com.joaodev.trackflowapi.order.event;

import java.time.LocalDateTime;

public record OrderStatusChangedEvent(
        Long orderId,
        String orderNumber,
        String previousStatus,
        String newStatus,
        LocalDateTime occurredAt
) {
}
