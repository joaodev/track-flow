package com.joaodev.trackflowapi.shipment.service;

import com.joaodev.trackflowapi.carrier.domain.Carrier;
import com.joaodev.trackflowapi.carrier.repository.CarrierRepository;
import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.domain.TrackingEvent;
import com.joaodev.trackflowapi.shipment.dto.CreateShipmentRequest;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
import com.joaodev.trackflowapi.shipment.event.ShipmentDeletedEvent;
import com.joaodev.trackflowapi.shipment.event.ShipmentStatusChangedEvent;
import com.joaodev.trackflowapi.shipment.repository.ShipmentRepository;
import com.joaodev.trackflowapi.shipment.repository.TrackingEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           TrackingEventRepository trackingEventRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Shipment createShipment(CreateShipmentRequest request) {

        LocalDateTime now = LocalDateTime.now();

        Shipment shipment = Shipment.builder()
                .trackingCode(generateTrackingCode())
                .origin(request.origin())
                .destination(request.destination())
                .carrier(request.carrierName())
                .status("CREATED")
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Shipment saved = shipmentRepository.save(shipment);

        trackingEventRepository.save(TrackingEvent.builder()
                .shipmentId(saved.getId())
                .status("CREATED")
                .location(request.origin())
                .description("Shipment registered")
                .occurredAt(now)
                .build());

        return saved;
    }

    @Transactional
    public Shipment updateStatus(String trackingCode, UpdateShipmentStatusRequest request) {
        Shipment shipment = findByTrackingCode(trackingCode);

        String previousStatus = shipment.getStatus();
        LocalDateTime now = LocalDateTime.now();

        shipment.setStatus(request.status());
        shipment.setUpdatedAt(now);
        Shipment saved = shipmentRepository.save(shipment);

        trackingEventRepository.save(TrackingEvent.builder()
                .shipmentId(saved.getId())
                .status(request.status())
                .location(request.location())
                .description(request.description())
                .occurredAt(now)
                .build());

        eventPublisher.publishEvent(new ShipmentStatusChangedEvent(
                saved.getId(),
                saved.getTrackingCode(),
                previousStatus,
                request.status(),
                request.location(),
                request.description(),
                now
        ));

        return saved;
    }

    @Transactional
    public void deleteShipment(String trackingCode) {
        Shipment shipment = findByTrackingCode(trackingCode);
        LocalDateTime now = LocalDateTime.now();

        shipment.setDeleted(true);
        shipment.setUpdatedAt(now);
        shipmentRepository.save(shipment);

        eventPublisher.publishEvent(new ShipmentDeletedEvent(
                shipment.getId(),
                shipment.getTrackingCode(),
                now
        ));
    }

    public Shipment findByTrackingCode(String trackingCode) {
        return shipmentRepository.findByTrackingCodeAndDeletedFalse(trackingCode)
                .orElseThrow(() -> new ShipmentNotFoundException(trackingCode));
    }

    public List<TrackingEvent> getHistory(Long shipmentId) {
        return trackingEventRepository.findByShipmentIdOrderByOccurredAtAscIdAsc(shipmentId);
    }

    public List<TrackingEvent> getHistoryByTrackingCode(String trackingCode) {
        Shipment shipment = findByTrackingCode(trackingCode);
        return getHistory(shipment.getId());
    }

    public List<Shipment> findAll() {
        return shipmentRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    public Shipment findById(Long id) {
        return shipmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ShipmentNotFoundException("id: " + id));
    }

    private String generateTrackingCode() {
        return "TF" + UUID.randomUUID().toString().substring(0, 10).toUpperCase().replace("-", "");
    }
}