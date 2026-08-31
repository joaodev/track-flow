package com.joaodev.trackflowapi.customer.service;

import com.joaodev.trackflowapi.customer.domain.Customer;
import com.joaodev.trackflowapi.customer.dto.CustomerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class CustomerServiceIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CustomerService customerService;

    private String uniqueEmail() {
        return "customer-" + UUID.randomUUID() + "@example.com";
    }

    private CustomerRequest sampleRequest() {
        return new CustomerRequest("Alice Johnson", uniqueEmail(), "555-0123", "789 Pine St");
    }

    @Test
    void createsActiveNonDeletedCustomer() {
        Customer created = customerService.createCustomer(sampleRequest());

        assertThat(created.getId()).isNotNull();
        assertThat(created.isActive()).isTrue();
        assertThat(created.isDeleted()).isFalse();
        assertThat(created.getName()).isEqualTo("Alice Johnson");
    }

    @Test
    void updateCustomerChangesAllFields() {
        Customer created = customerService.createCustomer(sampleRequest());

        CustomerRequest update = new CustomerRequest(
                "Alice J. Smith", uniqueEmail(), "555-9999", "321 Elm St");
        Customer updated = customerService.updateCustomer(created.getId(), update);

        assertThat(updated.getName()).isEqualTo("Alice J. Smith");
        assertThat(updated.getPhone()).isEqualTo("555-9999");
        assertThat(updated.getAddress()).isEqualTo("321 Elm St");
    }

    @Test
    void setActiveTogglesActiveFlag() {
        Customer created = customerService.createCustomer(sampleRequest());

        Customer deactivated = customerService.setActive(created.getId(), false);
        assertThat(deactivated.isActive()).isFalse();

        Customer reactivated = customerService.setActive(created.getId(), true);
        assertThat(reactivated.isActive()).isTrue();
    }

    @Test
    void deleteCustomerSoftDeletesAndExcludesFromFindAll() {
        Customer created = customerService.createCustomer(sampleRequest());

        customerService.deleteCustomer(created.getId());

        assertThat(customerService.findAll())
                .extracting(Customer::getId)
                .doesNotContain(created.getId());

        Customer stillFindable = customerService.findById(created.getId());
        assertThat(stillFindable.isDeleted()).isTrue();
    }

    @Test
    void findingNonExistentCustomerThrows() {
        assertThatThrownBy(() -> customerService.findById(-1L))
                .isInstanceOf(CustomerNotFoundException.class);
    }
}