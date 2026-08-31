package com.joaodev.trackflowapi.order.dto;

import jakarta.validation.constraints.NotBlank;

public record ShipOrderRequest(
        @NotBlank String carrier
) {
}
