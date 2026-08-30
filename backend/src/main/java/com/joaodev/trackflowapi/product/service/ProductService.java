package com.joaodev.trackflowapi.product.service;

import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.dto.CreateProductRequest;
import com.joaodev.trackflowapi.product.dto.UpdateProductRequest;
import com.joaodev.trackflowapi.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

        return productRepository.save(product);
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
        return productRepository.findAllByOrderByCreatedAtDesc();
    }
}
