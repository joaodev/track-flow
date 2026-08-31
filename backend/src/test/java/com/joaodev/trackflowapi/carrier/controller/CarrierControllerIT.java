package com.joaodev.trackflowapi.carrier.controller;

import com.joaodev.trackflowapi.auth.dto.AuthResponse;
import com.joaodev.trackflowapi.auth.dto.CreateUserRequest;
import com.joaodev.trackflowapi.auth.dto.LoginRequest;
import com.joaodev.trackflowapi.carrier.domain.Carrier;
import com.joaodev.trackflowapi.carrier.dto.CarrierRequest;
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
public class CarrierControllerIT {

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

    @Test
    void createCarrierViaRestEndpoint() {
        String token = adminToken();

        ResponseEntity<Carrier> response = restTemplate.exchange(
                "/api/carriers", HttpMethod.POST,
                withAuth(new CarrierRequest("Express Freight", "555-0400"), token), Carrier.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assert response.getBody() != null;
        assertThat(response.getBody().isActive()).isTrue();
    }

    @Test
    void creatingCarrierWithBlankNameReturnsValidationError() {
        String token = adminToken();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/carriers", HttpMethod.POST, withAuth(new CarrierRequest("", null), token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void updateCarrierViaRestEndpoint() {
        String token = adminToken();
        ResponseEntity<Carrier> created = restTemplate.exchange(
                "/api/carriers", HttpMethod.POST, withAuth(new CarrierRequest("Old Freight", null), token), Carrier.class);
        assert created.getBody() != null;

        ResponseEntity<Carrier> response = restTemplate.exchange(
                "/api/carriers/" + created.getBody().getId(), HttpMethod.PUT,
                withAuth(new CarrierRequest("New Freight", "555-1111"), token), Carrier.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getName()).isEqualTo("New Freight");
    }

    @Test
    void activateAndDeactivateCarrierViaRestEndpoints() {
        String token = adminToken();
        ResponseEntity<Carrier> created = restTemplate.exchange(
                "/api/carriers", HttpMethod.POST, withAuth(new CarrierRequest("Toggle Freight", null), token), Carrier.class);
        assert created.getBody() != null;
        Long id = created.getBody().getId();

        ResponseEntity<Carrier> deactivated = restTemplate.exchange(
                "/api/carriers/" + id + "/deactivate", HttpMethod.PATCH, withAuth(null, token), Carrier.class);
        assert deactivated.getBody() != null;
        assertThat(deactivated.getBody().isActive()).isFalse();

        ResponseEntity<Carrier> reactivated = restTemplate.exchange(
                "/api/carriers/" + id + "/activate", HttpMethod.PATCH, withAuth(null, token), Carrier.class);
        assert reactivated.getBody() != null;
        assertThat(reactivated.getBody().isActive()).isTrue();
    }

    @Test
    void adminCanDeleteCarrierAndItDisappearsFromList() {
        String token = adminToken();
        ResponseEntity<Carrier> created = restTemplate.exchange(
                "/api/carriers", HttpMethod.POST, withAuth(new CarrierRequest("Delete Freight", null), token), Carrier.class);
        assert created.getBody() != null;
        Long id = created.getBody().getId();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/carriers/" + id, HttpMethod.DELETE, withAuth(null, token), Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Carrier[]> listResponse = restTemplate.exchange(
                "/api/carriers", HttpMethod.GET, withAuth(null, token), Carrier[].class);
        assert listResponse.getBody() != null;
        assertThat(listResponse.getBody())
                .extracting(Carrier::getId)
                .doesNotContain(id);
    }

    @Test
    void nonAdminCannotDeleteCarrierAndReceivesAccessDeniedErrorCode() {
        String adminToken = adminToken();
        String opsEmail = "ops-carrier-" + UUID.randomUUID() + "@trackflow.dev";

        restTemplate.exchange("/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(opsEmail, "password123", "OPS"), adminToken), Object.class);
        AuthResponse opsLogin = restTemplate.postForObject(
                "/api/auth/login", new LoginRequest(opsEmail, "password123"), AuthResponse.class);
        assert opsLogin != null;

        ResponseEntity<Carrier> created = restTemplate.exchange(
                "/api/carriers", HttpMethod.POST, withAuth(new CarrierRequest("Protected Freight", null), adminToken), Carrier.class);
        assert created.getBody() != null;

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/carriers/" + created.getBody().getId(), HttpMethod.DELETE, withAuth(null, opsLogin.token()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void gettingNonExistentCarrierReturnsNotFoundWithErrorCode() {
        String token = adminToken();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/carriers/-1", HttpMethod.GET, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assert response.getBody() != null;
        assertThat(response.getBody().get("errorCode")).isEqualTo("CARRIER_NOT_FOUND");
    }

    @Test
    void listingCarriersRequiresAuthentication() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/carriers", Map.class);

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