package com.joaodev.trackflowapi.customer.controller;

import com.joaodev.trackflowapi.auth.dto.AuthResponse;
import com.joaodev.trackflowapi.auth.dto.CreateUserRequest;
import com.joaodev.trackflowapi.auth.dto.LoginRequest;
import com.joaodev.trackflowapi.customer.domain.Customer;
import com.joaodev.trackflowapi.customer.dto.CustomerRequest;
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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
public class CustomerControllerIT {

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

    private String uniqueEmail() {
        return "customer-" + UUID.randomUUID() + "@example.com";
    }

    private CustomerRequest sampleRequest() {
        return new CustomerRequest("Bob Wilson", uniqueEmail(), "555-0111", "12 Birch Ln");
    }

    @Test
    void createCustomerViaRestEndpoint() {
        String token = adminToken();

        ResponseEntity<Customer> response = restTemplate.exchange(
                "/api/customers", HttpMethod.POST, withAuth(sampleRequest(), token), Customer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assert response.getBody() != null;
        assertThat(response.getBody().isActive()).isTrue();
    }

    @Test
    void creatingCustomerWithInvalidEmailReturnsValidationError() {
        String token = adminToken();

        CustomerRequest invalid = new CustomerRequest("Bad Email", "not-an-email", "555-0000", "Addr");
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/customers", HttpMethod.POST, withAuth(invalid, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void updateCustomerViaRestEndpoint() {
        String token = adminToken();
        ResponseEntity<Customer> created = restTemplate.exchange(
                "/api/customers", HttpMethod.POST, withAuth(sampleRequest(), token), Customer.class);
        assert created.getBody() != null;

        CustomerRequest update = new CustomerRequest("Bob W. Wilson", uniqueEmail(), "555-2222", "New Addr");
        ResponseEntity<Customer> response = restTemplate.exchange(
                "/api/customers/" + created.getBody().getId(), HttpMethod.PUT, withAuth(update, token), Customer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getName()).isEqualTo("Bob W. Wilson");
    }

    @Test
    void activateAndDeactivateCustomerViaRestEndpoints() {
        String token = adminToken();
        ResponseEntity<Customer> created = restTemplate.exchange(
                "/api/customers", HttpMethod.POST, withAuth(sampleRequest(), token), Customer.class);
        assert created.getBody() != null;
        Long id = created.getBody().getId();

        ResponseEntity<Customer> deactivated = restTemplate.exchange(
                "/api/customers/" + id + "/deactivate", HttpMethod.PATCH, withAuth(null, token), Customer.class);
        assert deactivated.getBody() != null;
        assertThat(deactivated.getBody().isActive()).isFalse();

        ResponseEntity<Customer> reactivated = restTemplate.exchange(
                "/api/customers/" + id + "/activate", HttpMethod.PATCH, withAuth(null, token), Customer.class);
        assert reactivated.getBody() != null;
        assertThat(reactivated.getBody().isActive()).isTrue();
    }

    @Test
    void adminCanDeleteCustomerAndItDisappearsFromList() {
        String token = adminToken();
        ResponseEntity<Customer> created = restTemplate.exchange(
                "/api/customers", HttpMethod.POST, withAuth(sampleRequest(), token), Customer.class);
        assert created.getBody() != null;
        Long id = created.getBody().getId();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/customers/" + id, HttpMethod.DELETE, withAuth(null, token), Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Customer[]> listResponse = restTemplate.exchange(
                "/api/customers", HttpMethod.GET, withAuth(null, token), Customer[].class);
        assert listResponse.getBody() != null;
        assertThat(listResponse.getBody())
                .extracting(Customer::getId)
                .doesNotContain(id);
    }

    @Test
    void nonAdminCannotDeleteCustomerAndReceivesAccessDeniedErrorCode() {
        String adminToken = adminToken();
        String opsEmail = "ops-customer-" + UUID.randomUUID() + "@trackflow.dev";

        restTemplate.exchange("/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(opsEmail, "password123", "OPS"), adminToken), Object.class);
        AuthResponse opsLogin = restTemplate.postForObject(
                "/api/auth/login", new LoginRequest(opsEmail, "password123"), AuthResponse.class);
        assert opsLogin != null;

        ResponseEntity<Customer> created = restTemplate.exchange(
                "/api/customers", HttpMethod.POST, withAuth(sampleRequest(), adminToken), Customer.class);
        assert created.getBody() != null;

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/customers/" + created.getBody().getId(), HttpMethod.DELETE, withAuth(null, opsLogin.token()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void gettingNonExistentCustomerReturnsNotFoundWithErrorCode() {
        String token = adminToken();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/customers/-1", HttpMethod.GET, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("CUSTOMER_NOT_FOUND");
    }

    @Test
    void listingCustomersRequiresAuthentication() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/customers", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("UNAUTHENTICATED");
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