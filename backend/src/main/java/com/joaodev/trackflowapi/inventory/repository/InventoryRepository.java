package com.joaodev.trackflowapi.inventory.repository;

import com.joaodev.trackflowapi.inventory.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    List<Inventory> findAllByOrderByProductIdAsc();
}
