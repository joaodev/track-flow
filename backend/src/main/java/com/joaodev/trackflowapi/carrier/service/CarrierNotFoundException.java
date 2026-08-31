package com.joaodev.trackflowapi.carrier.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class CarrierNotFoundException extends RuntimeException implements ApiException {
    public CarrierNotFoundException(Long id) {
        super("No carrier found with id " + id);
    }

    @Override
    public String getErrorCode() {
        return "CARRIER_NOT_FOUND";
    }
}