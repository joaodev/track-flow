package com.joaodev.trackflowapi.inventory.service;

import com.joaodev.trackflowapi.inventory.domain.Inventory;
import com.joaodev.trackflowapi.inventory.dto.AdjustStockRequest;
import com.joaodev.trackflowapi.inventory.dto.UpdateThresholdRequest;
import com.joaodev.trackflowapi.inventory.event.LowStockEvent;
import com.joaodev.trackflowapi.inventory.repository.InventoryRepository;
import com.joaodev.trackflowapi.product.dto.UpdateProductRequest;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    private final InventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryService(InventoryRepository inventoryRepository, ApplicationEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Inventory createForProduct(Long productId, int initialQuantity) {
        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantityOnHand(initialQuantity)
                .quantityReserved(0)
                .lowStockThreshold(DEFAULT_LOW_STOCK_THRESHOLD)
                .updatedAt(LocalDateTime.now())
                .build();

        Inventory saved = inventoryRepository.save(inventory);
        publishLowStockIfNeeded(saved);
        return saved;
    }

    @Transactional
    public Inventory adjustStock(Long productId, AdjustStockRequest request) {
        Inventory inventory = findByProductId(productId);

        int newQuantity = inventory.getQuantityOnHand() + request.quantityDelta();
        if (newQuantity < 0) {
            throw new InsufficientStockException(productId);
        }

        inventory.setQuantityOnHand(newQuantity);
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory saved = inventoryRepository.save(inventory);

        publishLowStockIfNeeded(saved);

        return saved;
    }

    @Transactional
    public Inventory updateThreshold(Long productId, UpdateThresholdRequest request) {
        Inventory inventory = findByProductId(productId);
        inventory.setLowStockThreshold(request.lowStockThreshold());
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory saved = inventoryRepository.save(inventory);
        publishLowStockIfNeeded(saved);

        return saved;
    }

    @Transactional
    public Inventory reserve(Long productId, int quantity) {
        Inventory inventory = findByProductId(productId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(productId);
        }

        inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory saved = inventoryRepository.save(inventory);
        publishLowStockIfNeeded(saved);
        return saved;
    }

    @Transactional
    public Inventory release(Long productId, int quantity) {
        Inventory inventory = findByProductId(productId);
        inventory.setQuantityReserved(Math.max(0, inventory.getQuantityReserved() - quantity));
        inventory.setUpdatedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory fullFill(Long productId, int quantity) {
        Inventory inventory = findByProductId(productId);
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
        inventory.setQuantityReserved(Math.max(0, inventory.getQuantityReserved() - quantity));
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory saved = inventoryRepository.save(inventory);
        publishLowStockIfNeeded(saved);
        return saved;
    }

    public Inventory findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAllByOrderByProductIdAsc();
    }

    private void publishLowStockIfNeeded(Inventory inventory) {
        if (inventory.getAvailableQuantity() <= inventory.getLowStockThreshold()) {
            eventPublisher.publishEvent(new LowStockEvent(
                    inventory.getProductId(),
                    inventory.getAvailableQuantity(),
                    inventory.getLowStockThreshold(),
                    LocalDateTime.now()
            ));
        }
    }
}
