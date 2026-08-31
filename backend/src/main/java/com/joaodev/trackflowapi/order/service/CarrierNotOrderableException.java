package com.joaodev.trackflowapi.order.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class CarrierNotOrderableException extends RuntimeException implements ApiException {
    public CarrierNotOrderableException(Long carrierId) {
        super("Carrier " + carrierId + " is not active and cannot be used for shipping");
    }

    @Override
    public String getErrorCode() {
        return "CARRIER_NOT_ORDERABLE";
    }
}