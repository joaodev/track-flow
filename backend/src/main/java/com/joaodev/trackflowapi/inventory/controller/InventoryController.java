package com.joaodev.trackflowapi.inventory.controller;

import com.joaodev.trackflowapi.inventory.domain.Inventory;
import com.joaodev.trackflowapi.inventory.dto.AdjustStockRequest;
import com.joaodev.trackflowapi.inventory.dto.UpdateThresholdRequest;
import com.joaodev.trackflowapi.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Inventory> list() {
        return inventoryService.findAll();
    }

    @GetMapping("/{productId}")
    public Inventory get(@PathVariable Long productId) {
        return inventoryService.findByProductId(productId);
    }

    @PatchMapping("/{productId}/adjust")
    public Inventory adjust(@PathVariable Long productId, @Valid @RequestBody AdjustStockRequest request) {
        return inventoryService.adjustStock(productId, request);
    }

    @PatchMapping("/{productId}/threshold")
    public Inventory updateThreshold(@PathVariable Long productId, @Valid @RequestBody UpdateThresholdRequest request) {
        return inventoryService.updateThreshold(productId, request);
    }
}
