package com.joaodev.trackflowapi.carrier.service;

import com.joaodev.trackflowapi.carrier.domain.Carrier;
import com.joaodev.trackflowapi.carrier.dto.CarrierRequest;
import com.joaodev.trackflowapi.carrier.repository.CarrierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarrierService {

    private final CarrierRepository carrierRepository;

    public CarrierService(CarrierRepository carrierRepository) {
        this.carrierRepository = carrierRepository;
    }

    @Transactional
    public Carrier createCarrier(CarrierRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Carrier carrier = Carrier.builder()
                .name(request.name())
                .contactInfo(request.contactInfo())
                .active(true)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return carrierRepository.save(carrier);
    }

    @Transactional
    public Carrier updateCarrier(Long id, CarrierRequest request) {
        Carrier carrier = findById(id);
        carrier.setName(request.name());
        carrier.setContactInfo(request.contactInfo());
        carrier.setUpdatedAt(LocalDateTime.now());
        return carrierRepository.save(carrier);
    }

    @Transactional
    public Carrier setActive(Long id, boolean active) {
        Carrier carrier = findById(id);
        carrier.setActive(active);
        carrier.setUpdatedAt(LocalDateTime.now());
        return carrierRepository.save(carrier);
    }

    @Transactional
    public void deleteCarrier(Long id) {
        Carrier carrier = findById(id);
        carrier.setDeleted(true);
        carrier.setUpdatedAt(LocalDateTime.now());
        carrierRepository.save(carrier);
    }

    public Carrier findById(Long id) {
        return carrierRepository.findById(id)
                .orElseThrow(() -> new CarrierNotFoundException(id));
    }

    public List<Carrier> findAll() {
        return  carrierRepository.findByDeletedFalseOrderByNameAsc();
    }
}
