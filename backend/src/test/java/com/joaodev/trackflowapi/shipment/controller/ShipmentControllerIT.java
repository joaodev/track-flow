package com.joaodev.trackflowapi.shipment.controller;

import com.joaodev.trackflowapi.auth.dto.AuthResponse;
import com.joaodev.trackflowapi.auth.dto.CreateUserRequest;
import com.joaodev.trackflowapi.auth.dto.LoginRequest;
import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.dto.CreateShipmentRequest;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
import com.joaodev.trackflowapi.shipment.service.ShipmentService;
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

    @Autowired
    private ShipmentService shipmentService;

    private Shipment createShipment(String origin, String destination, String carrier) {
        return shipmentService.createShipment(new CreateShipmentRequest(origin, destination, carrier));
    }

    @Test
    void updateShipmentStatusViaRestEndpoint() {
        String token = adminToken();
        Shipment created = createShipment("Recife", "Fortaleza", "JALog");

        UpdateShipmentStatusRequest updateRequest = new UpdateShipmentStatusRequest(
                "IN_TRANSIT", "Recife Hub", "Departed origin facility");

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
    void listingShipmentsRequiresAuthentication() {
        ResponseEntity<?> response = restTemplate.getForEntity("/api/shipments", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticatedUserCanListShipments() {
        String token = adminToken();
        createShipment("Curitiba", "Joinville", "Correios");

        ResponseEntity<Shipment[]> response = restTemplate.exchange(
                "/api/shipments", HttpMethod.GET, withAuth(null, token), Shipment[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void adminCanDeleteShipment() {
        String token = adminToken();
        Shipment created = createShipment("Vitória", "Cariacica", "Correios");

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/shipments/" + created.getTrackingCode(), HttpMethod.DELETE, withAuth(null, token), Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<?> getResponse = restTemplate.getForEntity(
                "/api/shipments/" + created.getTrackingCode(), Map.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletedShipmentDoesNotAppearInList() {
        String token = adminToken();
        Shipment created = createShipment("Natal", "João Pessoa", "Jadlog");

        restTemplate.exchange("/api/shipments/" + created.getTrackingCode(), HttpMethod.DELETE, withAuth(null, token), Void.class);

        ResponseEntity<Shipment[]> listResponse = restTemplate.exchange(
                "/api/shipments", HttpMethod.GET, withAuth(null, token), Shipment[].class);

        assertThat(listResponse.getBody())
                .extracting(Shipment::getTrackingCode)
                .doesNotContain(created.getTrackingCode());
    }

    @Test
    void deletingNonExistentShipmentReturnsNotFound() {
        String token = adminToken();

        ResponseEntity<?> response = restTemplate.exchange(
                "/api/shipments/TFNOTFOUND01", HttpMethod.DELETE, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void nonAdminCannotDeleteShipment() {
        String adminToken = adminToken();
        String opsEmail = "ops-" + UUID.randomUUID() + "@trackflow.dev";

        restTemplate.exchange("/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(opsEmail, "password123", "OPS"), adminToken), Object.class);

        AuthResponse opsLogin = restTemplate.postForObject(
                "/api/auth/login", new LoginRequest(opsEmail, "password123"), AuthResponse.class);
        assert opsLogin != null;

        Shipment created = createShipment("Manaus", "Belém", "Correios");

        ResponseEntity<?> response = restTemplate.exchange(
                "/api/shipments/" + created.getTrackingCode(), HttpMethod.DELETE, withAuth(null, opsLogin.token()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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