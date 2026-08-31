package com.joaodev.trackflowapi.customer.service;

import com.joaodev.trackflowapi.customer.domain.Customer;
import com.joaodev.trackflowapi.customer.dto.CustomerRequest;
import com.joaodev.trackflowapi.customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(CustomerRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .active(true)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(Long id, CustomerRequest request) {
        Customer customer = findById(id);
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer setActive(Long id, boolean active) {
        Customer customer = findById(id);
        customer.setActive(active);
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findById(id);
        customer.setDeleted(true);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public List<Customer> findAll() {
        return customerRepository.findByDeletedFalseOrderByNameAsc();
    }
}
