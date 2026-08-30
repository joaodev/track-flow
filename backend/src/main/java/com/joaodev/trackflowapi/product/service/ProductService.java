package com.joaodev.trackflowapi.product.service;

import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.dto.CreateProductRequest;
import com.joaodev.trackflowapi.product.dto.UpdateProductRequest;
import com.joaodev.trackflowapi.product.event.ProductCreatedEvent;
import com.joaodev.trackflowapi.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductService(ProductRepository productRepository, ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Product createProduct(CreateProductRequest request) {
        productRepository.findBySku(request.sku()).ifPresent(existing -> {
            throw new SkuAlreadyExistsException(request.sku());
        });

        LocalDateTime now = LocalDateTime.now();

        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .unitPrice(request.unitPrice())
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Product saved = productRepository.save(product);

        int initialQuantity =  request.initialQuantity() != null ? request.initialQuantity() : 0;
        eventPublisher.publishEvent(new ProductCreatedEvent(saved.getId(), saved.getSku(), initialQuantity, now));

        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, UpdateProductRequest request) {
        Product product = findById(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setUnitPrice(request.unitPrice());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Transactional
    public Product setActive(Long id, boolean active) {
        Product product = findById(id);
        product.setActive(active);
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<Product> findAll() {
        return productRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findById(id);
        product.setUpdatedAt(LocalDateTime.now());
        product.setDeleted(true);
        productRepository.save(product);
    }
}
