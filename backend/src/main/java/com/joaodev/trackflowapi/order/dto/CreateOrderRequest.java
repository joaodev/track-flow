package com.joaodev.trackflowapi.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerName,
        @NotBlank String origin,
        @NotBlank String destination,
        @NotEmpty List<@Valid OrderItemRequest> items
) {
}