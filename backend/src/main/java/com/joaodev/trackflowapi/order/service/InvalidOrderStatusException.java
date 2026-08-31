package com.joaodev.trackflowapi.order.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class InvalidOrderStatusException extends RuntimeException implements ApiException {
    public InvalidOrderStatusException(String orderNumber, String currentStatus, String requiredStatus) {
        super("Order " + orderNumber + " is " + currentStatus + ", expected " + requiredStatus);
    }

    @Override
    public String getErrorCode() {
        return "INVALID_ORDER_STATUS";
    }
}
