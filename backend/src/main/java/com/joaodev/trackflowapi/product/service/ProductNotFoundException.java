package com.joaodev.trackflowapi.product.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class ProductNotFoundException extends RuntimeException implements ApiException {
    public ProductNotFoundException(Long id) {
        super("No product found with id " + id);
    }

    @Override
    public String getErrorCode() {
        return "PRODUCT_NOT_FOUND";
    }
}
