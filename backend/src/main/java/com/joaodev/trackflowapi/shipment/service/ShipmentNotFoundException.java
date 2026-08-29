package com.joaodev.trackflowapi.shipment.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class ShipmentNotFoundException extends RuntimeException implements ApiException {
    public ShipmentNotFoundException(String trackingCode) {
        super("No shipment found with tracking code " + trackingCode);
    }

    @Override
    public String getErrorCode() {
        return "SHIPMENT_NOT_FOUND";
    }
}
