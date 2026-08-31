package com.joaodev.trackflowapi.shipment.controller;

import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.domain.TrackingEvent;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
import com.joaodev.trackflowapi.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
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

    @DeleteMapping("/{trackingCode}")
    public ResponseEntity<Void> deleteShipment(@PathVariable String trackingCode) {
        shipmentService.deleteShipment(trackingCode);
        return ResponseEntity.noContent().build();
    }
}
