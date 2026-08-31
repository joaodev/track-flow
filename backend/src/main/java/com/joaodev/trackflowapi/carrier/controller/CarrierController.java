package com.joaodev.trackflowapi.carrier.controller;

import com.joaodev.trackflowapi.carrier.domain.Carrier;
import com.joaodev.trackflowapi.carrier.dto.CarrierRequest;
import com.joaodev.trackflowapi.carrier.service.CarrierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriers")
public class CarrierController {

    private final CarrierService carrierService;

    public CarrierController(CarrierService carrierService) {
        this.carrierService = carrierService;
    }

    @PostMapping
    public ResponseEntity<Carrier> create(@Valid @RequestBody CarrierRequest request) {
        Carrier created = carrierService.createCarrier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<Carrier> list() {
        return carrierService.findAll();
    }

    @GetMapping("/{id}")
    public Carrier get(@PathVariable Long id) {
        return carrierService.findById(id);
    }

    @PutMapping("/{id}")
    public Carrier update(@PathVariable Long id, @Valid @RequestBody CarrierRequest request) {
        return carrierService.updateCarrier(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public Carrier deactivate(@PathVariable Long id) {
        return carrierService.setActive(id, false);
    }

    @PatchMapping("/{id}/activate")
    public Carrier activate(@PathVariable Long id) {
        return carrierService.setActive(id, true);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carrierService.deleteCarrier(id);
        return ResponseEntity.noContent().build();
    }
}