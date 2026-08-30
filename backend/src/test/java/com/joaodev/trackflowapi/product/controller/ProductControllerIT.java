package com.joaodev.trackflowapi.product.controller;

import com.joaodev.trackflowapi.auth.dto.AuthResponse;
import com.joaodev.trackflowapi.auth.dto.CreateUserRequest;
import com.joaodev.trackflowapi.auth.dto.LoginRequest;
import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.dto.CreateProductRequest;
import com.joaodev.trackflowapi.product.dto.UpdateProductRequest;
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
public class ProductControllerIT {

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

    private String uniqueSku() {
        return "SKU-" + UUID.randomUUID();
    }

    @Test
    void createProductViaRestEndpoint() {
        String token = adminToken();

        CreateProductRequest request = new CreateProductRequest(
                uniqueSku(), "Desk Lamp", "LED, adjustable", new BigDecimal("89.90"), 20);

        ResponseEntity<Product> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST, withAuth(request, token), Product.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assert response.getBody() != null;
        assertThat(response.getBody().isActive()).isTrue();
    }

    @Test
    void creatingProductWithDuplicateSkuReturnsConflictWithErrorCode() {
        String token = adminToken();
        String sku = uniqueSku();

        restTemplate.exchange("/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(sku, "Chair", null, new BigDecimal("399.90"), 1), token),
                Product.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(sku, "Chair v2", null, new BigDecimal("429.90"), 1), token),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("SKU_ALREADY_EXISTS");
    }

    @Test
    void creatingProductWithInvalidDataReturnsBadRequestWithValidationErrorCode() {
        String token = adminToken();

        CreateProductRequest invalid = new CreateProductRequest("", "", null, new BigDecimal("-1"), null);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST, withAuth(invalid, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void gettingNonExistentProductReturnsNotFoundWithErrorCode() {
        String token = adminToken();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/products/-1", HttpMethod.GET, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void listingProductsRequiresAuthenticationAndReturnsUnauthenticatedErrorCode() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/products", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("UNAUTHENTICATED");
    }

    @Test
    void updateProductViaRestEndpoint() {
        String token = adminToken();

        ResponseEntity<Product> created = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(uniqueSku(), "Backpack", null, new BigDecimal("259.90"), 4), token),
                Product.class);

        assert created.getBody() != null;
        Long id = created.getBody().getId();

        UpdateProductRequest update = new UpdateProductRequest(
                "Backpack Pro", "Water-resistant", new BigDecimal("299.90"));

        ResponseEntity<Product> response = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.PUT, withAuth(update, token), Product.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getName()).isEqualTo("Backpack Pro");
    }

    @Test
    void activateAndDeactivateProductViaRestEndpoints() {
        String token = adminToken();

        ResponseEntity<Product> created = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(uniqueSku(), "Notebook", null, new BigDecimal("15.90"), 100), token),
                Product.class);

        assert created.getBody() != null;
        Long id = created.getBody().getId();

        ResponseEntity<Product> deactivated = restTemplate.exchange(
                "/api/products/" + id + "/deactivate", HttpMethod.PATCH, withAuth(null, token), Product.class);
        assert deactivated.getBody() != null;
        assertThat(deactivated.getBody().isActive()).isFalse();

        ResponseEntity<Product> reactivated = restTemplate.exchange(
                "/api/products/" + id + "/activate", HttpMethod.PATCH, withAuth(null, token), Product.class);
        assert reactivated.getBody() != null;
        assertThat(reactivated.getBody().isActive()).isTrue();
    }

    @Test
    void adminCanDeleteProductAndItDisappearsFromList() {
        String token = adminToken();

        ResponseEntity<Product> created = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(uniqueSku(), "Tripod", null, new BigDecimal("129.00"), 6), token),
                Product.class);

        assert created.getBody() != null;
        Long id = created.getBody().getId();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.DELETE, withAuth(null, token), Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Product[]> listResponse = restTemplate.exchange(
                "/api/products", HttpMethod.GET, withAuth(null, token), Product[].class);

        assert listResponse.getBody() != null;
        assertThat(listResponse.getBody())
                .extracting(Product::getId)
                .doesNotContain(id);
    }

    @Test
    void deletedProductIsStillReachableByIdForOtherModules() {
        String token = adminToken();

        ResponseEntity<Product> created = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(uniqueSku(), "Router", null, new BigDecimal("199.00"), 8), token),
                Product.class);

        assert created.getBody() != null;
        Long id = created.getBody().getId();

        restTemplate.exchange("/api/products/" + id, HttpMethod.DELETE, withAuth(null, token), Void.class);

        ResponseEntity<Product> getResponse = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.GET, withAuth(null, token), Product.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert getResponse.getBody() != null;
        assertThat(getResponse.getBody().isDeleted()).isTrue();
    }

    @Test
    void nonAdminCannotDeleteProductAndReceivesAccessDeniedErrorCode() {
        String adminToken = adminToken();
        String opsEmail = "ops-" + UUID.randomUUID() + "@trackflow.dev";

        restTemplate.exchange("/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(opsEmail, "password123", "OPS"), adminToken), Object.class);

        AuthResponse opsLogin = restTemplate.postForObject(
                "/api/auth/login", new LoginRequest(opsEmail, "password123"), AuthResponse.class);
        assert opsLogin != null;

        ResponseEntity<Product> created = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(uniqueSku(), "Cable Organizer", null, new BigDecimal("29.90"), 15), adminToken),
                Product.class);

        assert created.getBody() != null;
        Long id = created.getBody().getId();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.DELETE, withAuth(null, opsLogin.token()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void opsStaffCanCreateAndUpdateProducts() {
        String adminToken = adminToken();
        String opsEmail = "ops-" + UUID.randomUUID() + "@trackflow.dev";

        restTemplate.exchange("/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(opsEmail, "password123", "OPS"), adminToken), Object.class);

        AuthResponse opsLogin = restTemplate.postForObject(
                "/api/auth/login", new LoginRequest(opsEmail, "password123"), AuthResponse.class);
        assert opsLogin != null;

        ResponseEntity<Product> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                withAuth(new CreateProductRequest(uniqueSku(), "Mouse Pad", null, new BigDecimal("39.90"), 30), opsLogin.token()),
                Product.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
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