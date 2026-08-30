package com.joaodev.trackflowapi.inventory.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class InventoryNotFoundException extends RuntimeException implements ApiException {
    public InventoryNotFoundException(Long productId) {
        super("No inventory record found for product id " + productId);
    }

    @Override
    public String getErrorCode() {
        return "INVENTORY_NOT_FOUND";
    }
}
