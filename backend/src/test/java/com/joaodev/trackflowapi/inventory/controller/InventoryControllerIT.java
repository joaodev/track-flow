package com.joaodev.trackflowapi.inventory.controller;

import com.joaodev.trackflowapi.auth.dto.AuthResponse;
import com.joaodev.trackflowapi.auth.dto.LoginRequest;
import com.joaodev.trackflowapi.inventory.domain.Inventory;
import com.joaodev.trackflowapi.inventory.dto.AdjustStockRequest;
import com.joaodev.trackflowapi.inventory.dto.UpdateThresholdRequest;
import com.joaodev.trackflowapi.inventory.repository.InventoryRepository;
import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.dto.CreateProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
public class InventoryControllerIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    private String uniqueSku() {
        return "SKU-" + UUID.randomUUID();
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

    private Long createProductWithInventory(String token, int initialQuantity) {
        ResponseEntity<Product> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(uniqueSku(), "Inventory Test Product", null,
                        new BigDecimal("10.00"), initialQuantity), token),
                Product.class);

        assert response.getBody() != null;
        Long productId = response.getBody().getId();
        waitForInventory(productId);
        return productId;
    }

    @Test
    void listingInventoryRequiresAuthentication() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/inventory", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getInventoryForProductViaRestEndpoint() {
        String token = adminToken();
        Long productId = createProductWithInventory(token, 40);

        ResponseEntity<Inventory> response = restTemplate.exchange(
                "/api/inventory/" + productId, HttpMethod.GET, withAuth(null, token), Inventory.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getQuantityOnHand()).isEqualTo(40);
        assertThat(response.getBody().getAvailableQuantity()).isEqualTo(40);
    }

    @Test
    void gettingInventoryForNonExistentProductReturnsNotFoundWithErrorCode() {
        String token = adminToken();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/inventory/-1", HttpMethod.GET, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("INVENTORY_NOT_FOUND");
    }

    @Test
    void adjustStockViaRestEndpoint() {
        String token = adminToken();
        Long productId = createProductWithInventory(token, 20);

        ResponseEntity<Inventory> response = restTemplate.exchange(
                "/api/inventory/" + productId + "/adjust", HttpMethod.PATCH,
                withAuth(new AdjustStockRequest(10), token), Inventory.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getQuantityOnHand()).isEqualTo(30);
    }

    @Test
    void adjustingStockBelowZeroReturnsConflictWithErrorCode() {
        String token = adminToken();
        Long productId = createProductWithInventory(token, 5);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/inventory/" + productId + "/adjust", HttpMethod.PATCH,
                withAuth(new AdjustStockRequest(-10), token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    void updateThresholdViaRestEndpoint() {
        String token = adminToken();
        Long productId = createProductWithInventory(token, 60);

        ResponseEntity<Inventory> response = restTemplate.exchange(
                "/api/inventory/" + productId + "/threshold", HttpMethod.PATCH,
                withAuth(new UpdateThresholdRequest(15), token), Inventory.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getLowStockThreshold()).isEqualTo(15);
    }

    @Test
    void listInventoryIncludesCreatedProduct() {
        String token = adminToken();
        Long productId = createProductWithInventory(token, 12);

        ResponseEntity<Inventory[]> response = restTemplate.exchange(
                "/api/inventory", HttpMethod.GET, withAuth(null, token), Inventory[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody())
                .extracting(Inventory::getProductId)
                .contains(productId);
    }

    private String adminToken() {
        LoginRequest request = new LoginRequest("admin@trackflow.dev", "ChangeMe123!");
        AuthResponse response = restTemplate.postForObject("/api/auth/login", request, AuthResponse.class);
        assert response != null;
        return response.token();
    }

    private HttpEntity<Object> withAuth(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}