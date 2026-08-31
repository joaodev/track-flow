package com.joaodev.trackflowapi.order.controller;

import com.joaodev.trackflowapi.auth.dto.AuthResponse;
import com.joaodev.trackflowapi.auth.dto.CreateUserRequest;
import com.joaodev.trackflowapi.auth.dto.LoginRequest;
import com.joaodev.trackflowapi.inventory.repository.InventoryRepository;
import com.joaodev.trackflowapi.order.domain.Order;
import com.joaodev.trackflowapi.order.domain.OrderItem;
import com.joaodev.trackflowapi.order.dto.CreateOrderRequest;
import com.joaodev.trackflowapi.order.dto.OrderItemRequest;
import com.joaodev.trackflowapi.order.dto.ShipOrderRequest;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
public class OrderControllerIT {

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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Creates a product and waits for its (@Async) Inventory row before
     * returning — every order test needs stock to actually exist. */
    private Product createProductWithStock(String token, int initialQuantity) {
        ResponseEntity<Product> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(uniqueSku(), "Order Controller Test Product", null,
                        new BigDecimal("25.00"), initialQuantity), token),
                Product.class);

        assert response.getBody() != null;
        Product product = response.getBody();

        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            if (inventoryRepository.findByProductId(product.getId()).isPresent()) {
                return product;
            }
            sleep(50);
        }
        throw new AssertionError("Inventory was not created for product " + product.getId() + " within 5s");
    }

    private CreateOrderRequest orderRequestFor(Product product, int quantity) {
        return new CreateOrderRequest(
                "John Smith", "Warehouse B", "456 Oak Ave",
                List.of(new OrderItemRequest(product.getId(), quantity)));
    }

    private Order createOrder(String token, Product product, int quantity) {
        ResponseEntity<Order> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST, withAuth(orderRequestFor(product, quantity), token), Order.class);
        assert response.getBody() != null;
        return response.getBody();
    }

    @Test
    void createOrderViaRestEndpoint() {
        String token = adminToken();
        Product product = createProductWithStock(token, 30);

        ResponseEntity<Order> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST, withAuth(orderRequestFor(product, 4), token), Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assert response.getBody() != null;
        assertThat(response.getBody().getStatus()).isEqualTo("PENDING");
        assertThat(response.getBody().getOrderNumber()).startsWith("ORD");
    }

    @Test
    void creatingOrderWithNoItemsReturnsValidationError() {
        String token = adminToken();

        CreateOrderRequest invalid = new CreateOrderRequest("Jane", "A", "B", List.of());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST, withAuth(invalid, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void creatingOrderForInactiveProductReturnsConflictWithErrorCode() {
        String token = adminToken();
        Product product = createProductWithStock(token, 10);

        restTemplate.exchange("/api/products/" + product.getId() + "/deactivate",
                HttpMethod.PATCH, withAuth(null, token), Product.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST, withAuth(orderRequestFor(product, 1), token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("PRODUCT_NOT_ORDERABLE");
    }

    @Test
    void getOrderItemsViaRestEndpoint() {
        String token = adminToken();
        Product product = createProductWithStock(token, 15);
        Order order = createOrder(token, product, 3);

        ResponseEntity<OrderItem[]> response = restTemplate.exchange(
                "/api/orders/" + order.getId() + "/items", HttpMethod.GET, withAuth(null, token), OrderItem[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getQuantity()).isEqualTo(3);
    }

    @Test
    void confirmOrderViaRestEndpoint() {
        String token = adminToken();
        Product product = createProductWithStock(token, 20);
        Order order = createOrder(token, product, 5);

        ResponseEntity<Order> response = restTemplate.exchange(
                "/api/orders/" + order.getId() + "/confirm", HttpMethod.PATCH, withAuth(null, token), Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void confirmingAlreadyConfirmedOrderReturnsConflictWithErrorCode() {
        String token = adminToken();
        Product product = createProductWithStock(token, 20);
        Order order = createOrder(token, product, 2);

        restTemplate.exchange("/api/orders/" + order.getId() + "/confirm", HttpMethod.PATCH, withAuth(null, token), Order.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders/" + order.getId() + "/confirm", HttpMethod.PATCH, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("INVALID_ORDER_STATUS");
    }

    @Test
    void confirmingOrderWithInsufficientStockReturnsConflictWithErrorCode() {
        String token = adminToken();
        Product product = createProductWithStock(token, 2);
        Order order = createOrder(token, product, 10);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders/" + order.getId() + "/confirm", HttpMethod.PATCH, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    void shipOrderViaRestEndpoint() {
        String token = adminToken();
        Product product = createProductWithStock(token, 20);
        Order order = createOrder(token, product, 5);
        restTemplate.exchange("/api/orders/" + order.getId() + "/confirm", HttpMethod.PATCH, withAuth(null, token), Order.class);

        ResponseEntity<Order> response = restTemplate.exchange(
                "/api/orders/" + order.getId() + "/ship", HttpMethod.PATCH,
                withAuth(new ShipOrderRequest("Correios"), token), Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getStatus()).isEqualTo("SHIPPED");
        assertThat(response.getBody().getShipmentId()).isNotNull();
    }

    @Test
    void cancelOrderViaRestEndpoint() {
        String token = adminToken();
        Product product = createProductWithStock(token, 20);
        Order order = createOrder(token, product, 3);

        ResponseEntity<Order> response = restTemplate.exchange(
                "/api/orders/" + order.getId() + "/cancel", HttpMethod.PATCH, withAuth(null, token), Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void gettingNonExistentOrderReturnsNotFoundWithErrorCode() {
        String token = adminToken();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders/-1", HttpMethod.GET, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("ORDER_NOT_FOUND");
    }

    @Test
    void listingOrdersRequiresAuthentication() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/orders", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("UNAUTHENTICATED");
    }

    @Test
    void adminCanDeleteOrderAndItDisappearsFromList() {
        String token = adminToken();
        Product product = createProductWithStock(token, 10);
        Order order = createOrder(token, product, 1);

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/orders/" + order.getId(), HttpMethod.DELETE, withAuth(null, token), Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Order[]> listResponse = restTemplate.exchange(
                "/api/orders", HttpMethod.GET, withAuth(null, token), Order[].class);
        assert listResponse.getBody() != null;
        assertThat(listResponse.getBody())
                .extracting(Order::getId)
                .doesNotContain(order.getId());
    }

    @Test
    void nonAdminCannotDeleteOrderAndReceivesAccessDeniedErrorCode() {
        String adminToken = adminToken();
        String opsEmail = "ops-order-" + UUID.randomUUID() + "@trackflow.dev";

        restTemplate.exchange("/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(opsEmail, "password123", "OPS"), adminToken), Object.class);
        AuthResponse opsLogin = restTemplate.postForObject(
                "/api/auth/login", new LoginRequest(opsEmail, "password123"), AuthResponse.class);
        assert opsLogin != null;

        Product product = createProductWithStock(adminToken, 10);
        Order order = createOrder(adminToken, product, 1);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders/" + order.getId(), HttpMethod.DELETE, withAuth(null, opsLogin.token()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void opsStaffCanCreateAndConfirmOrders() {
        String adminToken = adminToken();
        String opsEmail = "ops-order2-" + UUID.randomUUID() + "@trackflow.dev";

        restTemplate.exchange("/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(opsEmail, "password123", "OPS"), adminToken), Object.class);
        AuthResponse opsLogin = restTemplate.postForObject(
                "/api/auth/login", new LoginRequest(opsEmail, "password123"), AuthResponse.class);
        assert opsLogin != null;

        Product product = createProductWithStock(adminToken, 10);

        // Order creation and confirmation are open to any authenticated
        // staff, unlike deletion, which is admin-only (tested above).
        ResponseEntity<Order> created = restTemplate.exchange(
                "/api/orders", HttpMethod.POST, withAuth(orderRequestFor(product, 2), opsLogin.token()), Order.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assert created.getBody() != null;
        ResponseEntity<Order> confirmed = restTemplate.exchange(
                "/api/orders/" + created.getBody().getId() + "/confirm", HttpMethod.PATCH,
                withAuth(null, opsLogin.token()), Order.class);
        assertThat(confirmed.getStatusCode()).isEqualTo(HttpStatus.OK);
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