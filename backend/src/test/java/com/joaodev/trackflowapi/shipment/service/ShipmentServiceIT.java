package com.joaodev.trackflowapi.shipment.service;

import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.domain.TrackingEvent;
import com.joaodev.trackflowapi.shipment.dto.CreateShipmentRequest;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
import com.joaodev.trackflowapi.shipment.event.ShipmentStatusChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@RecordApplicationEvents
public class ShipmentServiceIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void createsShipmentWithInitialTrackingEvent() {
        CreateShipmentRequest request = new CreateShipmentRequest(
                "São Paulo",
                "Rio de Janeiro",
                "Correios");

        Shipment created = shipmentService.createShipment(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTrackingCode()).startsWith("TF");
        assertThat(created.getStatus()).isEqualTo("CREATED");

        List<TrackingEvent> history = shipmentService.getHistory(created.getId());
        assertThat(history).hasSize(1);
        assertThat(history.getFirst().getStatus()).isEqualTo("CREATED");
    }

    @Test
    void updatingStatusAddsTrackingEventAndPublishesDomainEvent() {
        Shipment created = shipmentService.createShipment(
                new CreateShipmentRequest("Belo Horizonte", "Curitiba", "JALog"));

        UpdateShipmentStatusRequest update = new UpdateShipmentStatusRequest(
                "IN_TRANSIT", "Belo Horizonte Hub", "Department origin facility");

        Shipment updated = shipmentService.updateStatus(created.getTrackingCode(), update);

        assertThat(updated.getStatus()).isEqualTo("IN_TRANSIT");

        List<TrackingEvent> history = shipmentService.getHistory(updated.getId());
        assertThat(history).hasSize(2);
        assertThat(history.getLast().getStatus()).isEqualTo("IN_TRANSIT");

        List<ShipmentStatusChangedEvent> publishedEvents = applicationEvents
                .stream(ShipmentStatusChangedEvent.class)
                .toList();

        assertThat(publishedEvents).hasSize(1);
        assertThat(publishedEvents.getFirst().previousStatus()).isEqualTo("CREATED");
        assertThat(publishedEvents.getFirst().newStatus()).isEqualTo("IN_TRANSIT");
    }

    @Test
    void updatingNonExistingShipmentThrows() {
        UpdateShipmentStatusRequest update = new UpdateShipmentStatusRequest(
                "IN_TRANSIT", null, null);

        assertThatThrownBy(() -> shipmentService.updateStatus("TFNOTFOUND01", update))
                .isInstanceOf(ShipmentNotFoundException.class);
    }
}
