package com.joaodev.trackflowapi.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateThresholdRequest(
        @NotNull @Min(0) Integer lowStockThreshold
) {
}
