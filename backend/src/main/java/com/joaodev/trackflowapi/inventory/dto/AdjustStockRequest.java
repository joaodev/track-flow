package com.joaodev.trackflowapi.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(
        @NotNull Integer quantityDelta
) {
}
