package com.joaodev.trackflowapi.inventory.event;

import com.joaodev.trackflowapi.inventory.service.InventoryService;
import com.joaodev.trackflowapi.product.event.ProductCreatedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProductCreatedEventListener {

    private final InventoryService inventoryService;

    public ProductCreatedEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductCreated(ProductCreatedEvent event) {
        inventoryService.createForProduct(event.productId(), event.initialQuantity());
    }
}