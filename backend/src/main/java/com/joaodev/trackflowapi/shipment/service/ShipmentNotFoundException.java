package com.joaodev.trackflowapi.shipment.service;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(String trackingCode) {
        super("No shipment found with tracking code " + trackingCode);
    }
}
