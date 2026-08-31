package com.joaodev.trackflowapi.carrier.repository;

import com.joaodev.trackflowapi.carrier.domain.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {
    List<Carrier> findByDeletedFalseOrderByNameAsc();
}
