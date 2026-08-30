package com.joaodev.trackflowapi.inventory.websocket;

import com.joaodev.trackflowapi.inventory.event.LowStockEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class InventoryEventListener {

    private static final String DESTINATION = "/topic/inventory/low-stock";

    private final SimpMessagingTemplate messagingTemplate;

    public InventoryEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStock(LowStockEvent event) {
        log.info("Broadcasting low-stock alert for product {} to {}", event.productId(), DESTINATION);
        messagingTemplate.convertAndSend(DESTINATION, event);
    }
}
