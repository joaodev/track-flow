package com.joaodev.trackflowapi.carrier.service;

import com.joaodev.trackflowapi.carrier.domain.Carrier;
import com.joaodev.trackflowapi.carrier.dto.CarrierRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class CarrierServiceIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CarrierService carrierService;

    @Test
    void createsActiveNonDeletedCarrier() {
        Carrier created = carrierService.createCarrier(new CarrierRequest("Speedy Logistics", "555-0300"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.isActive()).isTrue();
        assertThat(created.isDeleted()).isFalse();
        assertThat(created.getName()).isEqualTo("Speedy Logistics");
    }

    @Test
    void contactInfoIsOptional() {
        Carrier created = carrierService.createCarrier(new CarrierRequest("No Contact Carrier", null));

        assertThat(created.getContactInfo()).isNull();
    }

    @Test
    void updateCarrierChangesFields() {
        Carrier created = carrierService.createCarrier(new CarrierRequest("Old Name", "555-0001"));

        Carrier updated = carrierService.updateCarrier(created.getId(),
                new CarrierRequest("New Name", "555-0002"));

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getContactInfo()).isEqualTo("555-0002");
    }

    @Test
    void setActiveTogglesActiveFlag() {
        Carrier created = carrierService.createCarrier(new CarrierRequest("Toggle Carrier", null));

        Carrier deactivated = carrierService.setActive(created.getId(), false);
        assertThat(deactivated.isActive()).isFalse();

        Carrier reactivated = carrierService.setActive(created.getId(), true);
        assertThat(reactivated.isActive()).isTrue();
    }

    @Test
    void deleteCarrierSoftDeletesAndExcludesFromFindAll() {
        Carrier created = carrierService.createCarrier(new CarrierRequest("Delete Me Carrier", null));

        carrierService.deleteCarrier(created.getId());

        assertThat(carrierService.findAll())
                .extracting(Carrier::getId)
                .doesNotContain(created.getId());

        Carrier stillFindable = carrierService.findById(created.getId());
        assertThat(stillFindable.isDeleted()).isTrue();
    }

    @Test
    void findingNonExistentCarrierThrows() {
        assertThatThrownBy(() -> carrierService.findById(-1L))
                .isInstanceOf(CarrierNotFoundException.class);
    }
}