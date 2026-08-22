package com.joaodev.trackflowapi.shipment.websocket;

import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.dto.CreateShipmentRequest;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
import com.joaodev.trackflowapi.shipment.event.ShipmentStatusChangedEvent;
import com.joaodev.trackflowapi.shipment.service.ShipmentService;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ShipmentWebSocketIT {

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
    private ShipmentService shipmentService;

    @Test
    void broadcastStatusChangeOverWebSocket() throws Exception {
        Shipment shipment = shipmentService.createShipment(
                new CreateShipmentRequest("Porto Alegre", "Florianópolis", "Correios"));

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        BlockingQueue<ShipmentStatusChangedEvent> receiveMessages = new LinkedBlockingDeque<>();

        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/shipments/" + shipment.getTrackingCode(), new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return ShipmentStatusChangedEvent.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                receiveMessages.add((ShipmentStatusChangedEvent) payload);
            }
        });

        Thread.sleep(500);

        shipmentService.updateStatus(shipment.getTrackingCode(),
                new UpdateShipmentStatusRequest(
                        "IN_TRANSIT", "Porto Alegre Hub", "Departed origin facility"));

        ShipmentStatusChangedEvent received = receiveMessages.poll(5, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.trackingCode()).isEqualTo(shipment.getTrackingCode());
        assertThat(received.newStatus()).isEqualTo("IN_TRANSIT");

        session.disconnect();
    }
}
