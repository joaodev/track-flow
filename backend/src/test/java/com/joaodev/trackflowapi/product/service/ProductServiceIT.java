package com.joaodev.trackflowapi.product.service;

import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.dto.CreateProductRequest;
import com.joaodev.trackflowapi.product.dto.UpdateProductRequest;
import com.joaodev.trackflowapi.product.event.ProductCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@RecordApplicationEvents
public class ProductServiceIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private ApplicationEvents applicationEvents;

    private String uniqueSku() {
        return "SKU-" + UUID.randomUUID();
    }

    @Test
    void createsProductAndPublishesProductCreatedEvent() {
        String sku = uniqueSku();
        CreateProductRequest request = new CreateProductRequest(
                sku, "Wireless Mouse", "Ergonomic, 2.4GHz", new BigDecimal("129.90"), 50);

        Product created = productService.createProduct(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getSku()).isEqualTo(sku);
        assertThat(created.isActive()).isTrue();
        assertThat(created.isDeleted()).isFalse();

        List<ProductCreatedEvent> published = applicationEvents
                .stream(ProductCreatedEvent.class)
                .toList();

        assertThat(published).hasSize(1);
        assertThat(published.getFirst().productId()).isEqualTo(created.getId());
        assertThat(published.getFirst().initialQuantity()).isEqualTo(50);
    }

    @Test
    void creatingProductWithoutInitialQuantityDefaultsEventQuantityToZero() {
        CreateProductRequest request = new CreateProductRequest(
                uniqueSku(), "USB Cable", null, new BigDecimal("19.90"), null);

        Product created = productService.createProduct(request);

        ProductCreatedEvent event = applicationEvents
                .stream(ProductCreatedEvent.class)
                .filter(e -> e.productId().equals(created.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(event.initialQuantity()).isZero();
    }

    @Test
    void creatingProductWithDuplicateSkuThrows() {
        String sku = uniqueSku();
        productService.createProduct(
                new CreateProductRequest(sku, "Keyboard", null, new BigDecimal("199.90"), 10));

        assertThatThrownBy(() ->
                productService.createProduct(
                        new CreateProductRequest(sku, "Keyboard v2", null, new BigDecimal("219.90"), 5)))
                .isInstanceOf(SkuAlreadyExistsException.class);
    }

    @Test
    void updateProductChangesNameDescriptionAndPriceButNotSku() {
        Product created = productService.createProduct(
                new CreateProductRequest(uniqueSku(), "Monitor", "24 inch", new BigDecimal("899.00"), 5));

        UpdateProductRequest update = new UpdateProductRequest(
                "Monitor 27\"", "27 inch, IPS", new BigDecimal("1099.00"));

        Product updated = productService.updateProduct(created.getId(), update);

        assertThat(updated.getName()).isEqualTo("Monitor 27\"");
        assertThat(updated.getDescription()).isEqualTo("27 inch, IPS");
        assertThat(updated.getUnitPrice()).isEqualByComparingTo("1099.00");
        assertThat(updated.getSku()).isEqualTo(created.getSku());
    }

    @Test
    void setActiveTogglesActiveFlag() {
        Product created = productService.createProduct(
                new CreateProductRequest(uniqueSku(), "Webcam", null, new BigDecimal("249.90"), 3));

        Product deactivated = productService.setActive(created.getId(), false);
        assertThat(deactivated.isActive()).isFalse();

        Product reactivated = productService.setActive(created.getId(), true);
        assertThat(reactivated.isActive()).isTrue();
    }

    @Test
    void deleteProductSoftDeletesAndExcludesFromFindAll() {
        Product created = productService.createProduct(
                new CreateProductRequest(uniqueSku(), "Headset", null, new BigDecimal("349.90"), 2));

        productService.deleteProduct(created.getId());

        assertThat(productService.findAll())
                .extracting(Product::getId)
                .doesNotContain(created.getId());

        Product stillFindable = productService.findById(created.getId());
        assertThat(stillFindable.isDeleted()).isTrue();
    }

    @Test
    void findingNonExistentProductThrows() {
        assertThatThrownBy(() -> productService.findById(-1L))
                .isInstanceOf(ProductNotFoundException.class);
    }
}