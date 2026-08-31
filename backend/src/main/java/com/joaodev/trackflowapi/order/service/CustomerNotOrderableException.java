package com.joaodev.trackflowapi.order.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class CustomerNotOrderableException extends RuntimeException implements ApiException {
    public CustomerNotOrderableException(Long customerId) {
        super("Customer " + customerId + " is not active and cannot place orders");
    }

    @Override
    public String getErrorCode() {
        return "CUSTOMER_NOT_ORDERABLE";
    }
}