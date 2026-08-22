package com.joaodev.trackflowapi.shipment.websocket;

import com.joaodev.trackflowapi.shipment.event.ShipmentStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ShipmentEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public ShipmentEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        String destination = "/topic/shipments/" + event.trackingCode();
        log.info("Broadcasting status changed for {} to {}", event.trackingCode(), destination);
        messagingTemplate.convertAndSend(destination, event);
    }
}
