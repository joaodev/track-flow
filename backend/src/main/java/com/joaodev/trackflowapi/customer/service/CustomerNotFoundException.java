package com.joaodev.trackflowapi.customer.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class CustomerNotFoundException extends RuntimeException implements ApiException {
    public CustomerNotFoundException(Long id) {
        super("No customer found with id " + id);
    }

    @Override
    public String getErrorCode() {
        return "CUSTOMER_NOT_FOUND";
    }
}
