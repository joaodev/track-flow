package com.joaodev.trackflowapi.order.dto;

import jakarta.validation.constraints.NotNull;

public record ShipOrderRequest(
        @NotNull Long carrierId
) {
}
