package com.joaodev.trackflowapi.product.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class SkuAlreadyExistsException extends RuntimeException implements ApiException {
    public SkuAlreadyExistsException(String sku) {
        super("A product with SKU " + sku + " already exists");
    }

    @Override
    public String getErrorCode() {
        return "SKU_ALREADY_EXISTS";
    }
}
