package com.joaodev.trackflowapi.inventory.websocket;

import com.joaodev.trackflowapi.inventory.domain.Inventory;
import com.joaodev.trackflowapi.inventory.dto.AdjustStockRequest;
import com.joaodev.trackflowapi.inventory.dto.UpdateThresholdRequest;
import com.joaodev.trackflowapi.inventory.event.LowStockEvent;
import com.joaodev.trackflowapi.inventory.repository.InventoryRepository;
import com.joaodev.trackflowapi.inventory.service.InventoryService;
import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.dto.CreateProductRequest;
import com.joaodev.trackflowapi.product.service.ProductService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class InventoryWebSocketIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

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
    void broadcastLowStockAlertOverWebSocket() throws Exception {
        Product product = productService.createProduct(new CreateProductRequest(
                "SKU-" + UUID.randomUUID(), "WS Test Product", null, new BigDecimal("10.00"), 100));
        waitForInventory(product.getId());
        inventoryService.updateThreshold(product.getId(), new UpdateThresholdRequest(10));

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        BlockingQueue<LowStockEvent> receivedMessages = new LinkedBlockingDeque<>();

        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/inventory/low-stock", new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return LowStockEvent.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                receivedMessages.add((LowStockEvent) payload);
            }
        });

        Thread.sleep(500);

        inventoryService.adjustStock(product.getId(), new AdjustStockRequest(-90));

        LowStockEvent received = receivedMessages.poll(5, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.productId()).isEqualTo(product.getId());
        assertThat(received.availableQuantity()).isEqualTo(10);
        assertThat(received.threshold()).isEqualTo(10);

        session.disconnect();
    }
}