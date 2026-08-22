package com.joaodev.trackflowapi.shipment.controller;

import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.dto.CreateShipmentRequest;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        CreateShipmentRequest request = new CreateShipmentRequest(
                "São Paulo", "Salvador", "Correios");

        ResponseEntity<Shipment> response = restTemplate.postForEntity(
                "/api/shipments", request, Shipment.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assert response.getBody() != null;
        assertThat(response.getBody().getStatus()).isEqualTo("CREATED");
    }

    @Test
    void updateShipmentStatusViaRestEndpoint() {
        CreateShipmentRequest createRequest = new CreateShipmentRequest(
                "Recife", "Fortaleza", "JALog");

        Shipment created = restTemplate.postForObject("/api/shipments", createRequest, Shipment.class);

        UpdateShipmentStatusRequest updateRequest = new UpdateShipmentStatusRequest(
                "IN_TRANSIT", "Recife Hub", "Departed origin facility");

        assert created != null;
        ResponseEntity<Shipment> response = restTemplate.exchange(
                "/api/shipments/" + created.getTrackingCode() + "/status",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
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
        CreateShipmentRequest invalid = new CreateShipmentRequest("", "", "");
        ResponseEntity<?> response = restTemplate.postForEntity(
                "/api/shipments", invalid, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
