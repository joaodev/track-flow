package com.joaodev.trackflowapi.shipment.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateShipmentStatusRequest(
        @NotBlank String status,
        String location,
        String description
) {
}
