package com.joaodev.trackflowapi.shipment.controller;

import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.domain.TrackingEvent;
import com.joaodev.trackflowapi.shipment.dto.CreateShipmentRequest;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
import com.joaodev.trackflowapi.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<Shipment> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        Shipment created = shipmentService.createShipment(request);
        return ResponseEntity
                .created(URI.create("/api/shipments/" + created.getTrackingCode()))
                .body(created);
    }

    @GetMapping("/{trackingCode}")
    public Shipment getShipment(@PathVariable String trackingCode) {
        return shipmentService.findByTrackingCode(trackingCode);
    }

    @PutMapping("/{trackingCode}/status")
    public Shipment updateStatus(@PathVariable String trackingCode,
                                 @Valid @RequestBody UpdateShipmentStatusRequest request) {
        return shipmentService.updateStatus(trackingCode, request);
    }

    @GetMapping("/{trackingCode}/history")
    public List<TrackingEvent> getHistory(@PathVariable String trackingCode) {
        return shipmentService.getHistoryByTrackingCode(trackingCode);
    }

    @GetMapping
    public List<Shipment> listShipments() {
        return shipmentService.findAll();
    }
}
