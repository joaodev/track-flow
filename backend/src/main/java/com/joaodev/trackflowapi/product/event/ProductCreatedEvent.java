package com.joaodev.trackflowapi.product.event;

import java.time.LocalDateTime;

public record ProductCreatedEvent(
        Long productId,
        String sku,
        int initialQuantity,
        LocalDateTime occurredAt
) {
}
