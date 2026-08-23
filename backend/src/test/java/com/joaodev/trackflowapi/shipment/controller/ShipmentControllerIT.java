package com.joaodev.trackflowapi.shipment.controller;

import com.joaodev.trackflowapi.auth.dto.AuthResponse;
import com.joaodev.trackflowapi.auth.dto.LoginRequest;
import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.dto.CreateShipmentRequest;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
public class ShipmentControllerIT {

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
    void createShipmentViaRestEndpoint() {
        String token = adminToken();

        CreateShipmentRequest request = new CreateShipmentRequest(
                "São Paulo", "Salvador", "Correios");

        ResponseEntity<Shipment> response = restTemplate.exchange(
                "/api/shipments", HttpMethod.POST, withAuth(request, token), Shipment.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assert response.getBody() != null;
        assertThat(response.getBody().getStatus()).isEqualTo("CREATED");
    }

    @Test
    void updateShipmentStatusViaRestEndpoint() {
        String token = adminToken();

        CreateShipmentRequest createRequest = new CreateShipmentRequest(
                "Recife", "Fortaleza", "JALog");

        ResponseEntity<Shipment> createResponse = restTemplate.exchange(
                "/api/shipments", HttpMethod.POST, withAuth(createRequest, token), Shipment.class);
        Shipment created = createResponse.getBody();

        UpdateShipmentStatusRequest updateRequest = new UpdateShipmentStatusRequest(
                "IN_TRANSIT", "Recife Hub", "Departed origin facility");

        assert created != null;
        ResponseEntity<Shipment> response = restTemplate.exchange(
                "/api/shipments/" + created.getTrackingCode() + "/status",
                HttpMethod.PUT,
                withAuth(updateRequest, token),
                Shipment.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getStatus()).isEqualTo("IN_TRANSIT");
    }

    @Test
    void gettingNonExistentShipmentReturnsNotFound() {
        ResponseEntity<?> response = restTemplate.getForEntity("/api/shipments/TFNOTFOUND01", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void creatingShipmentWithInvalidDataReturnsBadRequest() {
        String token = adminToken();

        CreateShipmentRequest invalid = new CreateShipmentRequest("", "", "");
        ResponseEntity<?> response = restTemplate.exchange(
                "/api/shipments", HttpMethod.POST, withAuth(invalid, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listingShipmentsRequiresAuthentication() {
        ResponseEntity<?> response = restTemplate.getForEntity("/api/shipments", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticatedUserCanListShipments() {
        String token = adminToken();

        restTemplate.exchange("/api/shipments", HttpMethod.POST,
                withAuth(new CreateShipmentRequest(
                        "Curitiba", "Joinville", "Correios"), token), Shipment.class);

        ResponseEntity<Shipment[]> response = restTemplate.exchange(
                "/api/shipments", HttpMethod.GET, withAuth(null, token), Shipment[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
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
