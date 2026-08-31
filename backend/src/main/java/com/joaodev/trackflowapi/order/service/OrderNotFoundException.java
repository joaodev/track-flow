package com.joaodev.trackflowapi.order.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class OrderNotFoundException extends RuntimeException implements ApiException {
    public OrderNotFoundException(Long id) {
        super("No order found with id " + id);
    }

    @Override
    public String getErrorCode() {
        return "ORDER_NOT_FOUND";
    }
}
