package com.joaodev.trackflowapi.product.controller;

import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.dto.CreateProductRequest;
import com.joaodev.trackflowapi.product.dto.UpdateProductRequest;
import com.joaodev.trackflowapi.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody CreateProductRequest request) {
        Product created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<Product> list() {
        return  productService.findAll();
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public Product deactivate(@PathVariable Long id) {
        return productService.setActive(id, false);
    }

    @PatchMapping("/{id}/activate")
    public Product activate(@PathVariable Long id) {
        return productService.setActive(id, true);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
