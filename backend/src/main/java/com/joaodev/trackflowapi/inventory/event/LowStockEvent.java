package com.joaodev.trackflowapi.inventory.event;

import java.time.LocalDateTime;

public record LowStockEvent(
        Long productId,
        int availableQuantity,
        int threshold,
        LocalDateTime occurredAt
) {
}
