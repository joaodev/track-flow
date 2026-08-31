package com.joaodev.trackflowapi.carrier.dto;

import jakarta.validation.constraints.NotBlank;

public record CarrierRequest(
        @NotBlank String name,
        String contactInfo
) {
}
