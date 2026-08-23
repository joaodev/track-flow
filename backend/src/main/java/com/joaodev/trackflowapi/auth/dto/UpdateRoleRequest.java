package com.joaodev.trackflowapi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateRoleRequest(
        @NotBlank @Pattern(regexp = "ADMIN|OPS") String role
) {
}
