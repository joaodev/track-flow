package com.joaodev.trackflowapi.order.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class ProductNotOrderableException extends RuntimeException implements ApiException {
    public ProductNotOrderableException(Long productId) {
        super("Product " + productId + " is not active and cannot be ordered");
    }

    @Override
    public String getErrorCode() {
        return "PRODUCT_NOT_ORDERABLE";
    }
}
