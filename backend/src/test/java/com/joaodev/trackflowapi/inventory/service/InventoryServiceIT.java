package com.joaodev.trackflowapi.inventory.service;

import com.joaodev.trackflowapi.inventory.domain.Inventory;
import com.joaodev.trackflowapi.inventory.dto.AdjustStockRequest;
import com.joaodev.trackflowapi.inventory.dto.UpdateThresholdRequest;
import com.joaodev.trackflowapi.inventory.event.LowStockEvent;
import com.joaodev.trackflowapi.inventory.repository.InventoryRepository;
import com.joaodev.trackflowapi.product.dto.CreateProductRequest;
import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@RecordApplicationEvents
public class InventoryServiceIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    private String uniqueSku() {
        return "SKU-" + UUID.randomUUID();
    }

    private Product createProduct(int initialQuantity) {
        return productService.createProduct(
                new CreateProductRequest(uniqueSku(), "Test Product", null, new BigDecimal("10.00"), initialQuantity));
    }

    private Inventory waitForInventory(Long productId) {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            var found = inventoryRepository.findByProductId(productId);
            if (found.isPresent()) {
                return found.get();
            }
            sleep(50);
        }
        throw new AssertionError("Inventory was not created for product " + productId + " within 5s");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void creatingProductEventuallyCreatesMatchingInventoryRow() {
        Product product = createProduct(50);

        Inventory inventory = waitForInventory(product.getId());

        assertThat(inventory.getProductId()).isEqualTo(product.getId());
        assertThat(inventory.getQuantityOnHand()).isEqualTo(50);
        assertThat(inventory.getQuantityReserved()).isZero();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(50);
    }

    @Test
    void adjustStockIncreasesQuantityOnHand() {
        Product product = createProduct(10);
        waitForInventory(product.getId());

        Inventory adjusted = inventoryService.adjustStock(product.getId(), new AdjustStockRequest(15));

        assertThat(adjusted.getQuantityOnHand()).isEqualTo(25);
    }

    @Test
    void adjustStockDecreasesQuantityOnHand() {
        Product product = createProduct(30);
        waitForInventory(product.getId());

        Inventory adjusted = inventoryService.adjustStock(product.getId(), new AdjustStockRequest(-10));

        assertThat(adjusted.getQuantityOnHand()).isEqualTo(20);
    }

    @Test
    void adjustStockRejectingNegativeResultThrowsInsufficientStock() {
        Product product = createProduct(5);
        waitForInventory(product.getId());

        assertThatThrownBy(() ->
                inventoryService.adjustStock(product.getId(), new AdjustStockRequest(-6)))
                .isInstanceOf(InsufficientStockException.class);

        Inventory unchanged = inventoryService.findByProductId(product.getId());
        assertThat(unchanged.getQuantityOnHand()).isEqualTo(5);
    }

    @Test
    void updateThresholdChangesLowStockThreshold() {
        Product product = createProduct(100);
        waitForInventory(product.getId());

        Inventory updated = inventoryService.updateThreshold(product.getId(), new UpdateThresholdRequest(25));

        assertThat(updated.getLowStockThreshold()).isEqualTo(25);
    }

    @Test
    void adjustingStockBelowThresholdPublishesLowStockEvent() {
        Product product = createProduct(100);
        waitForInventory(product.getId());
        inventoryService.updateThreshold(product.getId(), new UpdateThresholdRequest(10));

        // Drop available quantity to exactly the threshold — should trigger the alert.
        inventoryService.adjustStock(product.getId(), new AdjustStockRequest(-90));

        List<LowStockEvent> published = applicationEvents
                .stream(LowStockEvent.class)
                .filter(e -> e.productId().equals(product.getId()))
                .toList();

        assertThat(published).isNotEmpty();
        LowStockEvent lastAlert = published.getLast();
        assertThat(lastAlert.availableQuantity()).isEqualTo(10);
        assertThat(lastAlert.threshold()).isEqualTo(10);
    }

    @Test
    void adjustingStockWellAboveThresholdDoesNotPublishLowStockEvent() {
        Product product = createProduct(100);
        waitForInventory(product.getId());
        inventoryService.updateThreshold(product.getId(), new UpdateThresholdRequest(10));

        inventoryService.adjustStock(product.getId(), new AdjustStockRequest(-5));

        boolean anyAlertForThisProduct = applicationEvents
                .stream(LowStockEvent.class)
                .anyMatch(e -> e.productId().equals(product.getId()));

        assertThat(anyAlertForThisProduct).isFalse();
    }

    @Test
    void findingInventoryForNonExistentProductThrows() {
        assertThatThrownBy(() -> inventoryService.findByProductId(-1L))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void concurrentModificationsToSameInventoryRowTriggerOptimisticLockingConflict() {
        Product product = createProduct(20);
        Inventory original = waitForInventory(product.getId());

        Inventory copyA = inventoryRepository.findByProductId(original.getProductId()).orElseThrow();
        Inventory copyB = inventoryRepository.findByProductId(original.getProductId()).orElseThrow();

        copyA.setQuantityOnHand(copyA.getQuantityOnHand() - 5);
        inventoryRepository.saveAndFlush(copyA);

        copyB.setQuantityOnHand(copyB.getQuantityOnHand() - 3);

        assertThatThrownBy(() -> inventoryRepository.saveAndFlush(copyB))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        Inventory afterConflict = inventoryService.findByProductId(product.getId());
        assertThat(afterConflict.getQuantityOnHand()).isEqualTo(15);
    }
}