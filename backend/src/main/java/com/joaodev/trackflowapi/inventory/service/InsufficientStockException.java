package com.joaodev.trackflowapi.inventory.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class InsufficientStockException extends RuntimeException implements ApiException {
    public InsufficientStockException(Long productId) {
        super("Adjustment would take stock below zero for product id " + productId);
    }

    @Override
    public String getErrorCode() {
        return "INSUFFICIENT_STOCK";
    }
}
