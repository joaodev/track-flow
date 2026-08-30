package com.joaodev.trackflowapi.inventory.event;

import com.joaodev.trackflowapi.inventory.service.InventoryService;
import com.joaodev.trackflowapi.product.event.ProductCreatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProductCreatedEventListener {

    private final InventoryService inventoryService;

    public ProductCreatedEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductCreated(ProductCreatedEvent event) {
        inventoryService.createForProduct(event.productId());
    }
}
