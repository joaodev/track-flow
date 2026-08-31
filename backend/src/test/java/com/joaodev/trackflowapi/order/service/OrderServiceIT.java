package com.joaodev.trackflowapi.order.service;

import com.joaodev.trackflowapi.carrier.domain.Carrier;
import com.joaodev.trackflowapi.carrier.dto.CarrierRequest;
import com.joaodev.trackflowapi.carrier.service.CarrierService;
import com.joaodev.trackflowapi.customer.domain.Customer;
import com.joaodev.trackflowapi.customer.dto.CustomerRequest;
import com.joaodev.trackflowapi.customer.service.CustomerService;
import com.joaodev.trackflowapi.inventory.domain.Inventory;
import com.joaodev.trackflowapi.inventory.repository.InventoryRepository;
import com.joaodev.trackflowapi.order.domain.Order;
import com.joaodev.trackflowapi.order.domain.OrderItem;
import com.joaodev.trackflowapi.order.dto.CreateOrderRequest;
import com.joaodev.trackflowapi.order.dto.OrderItemRequest;
import com.joaodev.trackflowapi.order.dto.ShipOrderRequest;
import com.joaodev.trackflowapi.order.event.OrderStatusChangedEvent;
import com.joaodev.trackflowapi.order.repository.OrderRepository;
import com.joaodev.trackflowapi.product.domain.Product;
import com.joaodev.trackflowapi.product.dto.CreateProductRequest;
import com.joaodev.trackflowapi.product.service.ProductService;
import com.joaodev.trackflowapi.shipment.domain.Shipment;
import com.joaodev.trackflowapi.shipment.dto.UpdateShipmentStatusRequest;
import com.joaodev.trackflowapi.shipment.service.ShipmentService;
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

/**
 * Covers the Order state machine end to end: PENDING -> CONFIRMED -> SHIPPED
 * -> DELIVERED, and CANCELLED from each of the first three. Since OrderService
 * calls ProductService/InventoryService/ShipmentService/CustomerService/
 * CarrierService directly (not via events), those side effects are asserted
 * synchronously. The one exception is the SHIPPED -> DELIVERED transition,
 * which happens through ShipmentDeliveredEventListener — @Async, like
 * ProductCreatedEventListener — so that one test polls instead of asserting
 * immediately.
 */
@SpringBootTest
@Testcontainers
@RecordApplicationEvents
public class OrderServiceIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CarrierService carrierService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ApplicationEvents applicationEvents;

    private String uniqueSku() {
        return "SKU-" + UUID.randomUUID();
    }

    private Product createActiveProduct(int initialQuantity) {
        Product product = productService.createProduct(
                new CreateProductRequest(uniqueSku(), "Order Test Product", null, new BigDecimal("50.00"), initialQuantity));
        waitForInventory(product.getId());
        return product;
    }

    private Customer createActiveCustomer() {
        return customerService.createCustomer(new CustomerRequest(
                "Jane Doe", "jane-" + UUID.randomUUID() + "@example.com", "555-0100", "123 Main St"));
    }

    private Carrier createActiveCarrier() {
        return carrierService.createCarrier(new CarrierRequest("FastCarrier", "555-0200"));
    }

    private Inventory waitForInventory(Long productId) {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            var found = inventoryRepository.findByProductId(productId);
            if (found.isPresent()) {
                return found.get();
            }
            sleep(50);
        }
        throw new AssertionError("Inventory was not created for product " + productId + " within 5s");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private CreateOrderRequest orderFor(Customer customer, Product product, int quantity) {
        return new CreateOrderRequest(
                customer.getId(), "Warehouse A", "123 Main St",
                List.of(new OrderItemRequest(product.getId(), quantity)));
    }

    @Test
    void createOrderStartsAsPendingWithoutReservingStock() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);

        Order order = orderService.createOrder(orderFor(customer, product, 5));

        assertThat(order.getStatus()).isEqualTo("PENDING");
        assertThat(order.getCustomerId()).isEqualTo(customer.getId());
        assertThat(order.getShipmentId()).isNull();

        List<OrderItem> items = orderService.getItems(order.getId());
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().getQuantity()).isEqualTo(5);
        assertThat(items.getFirst().getUnitPriceAtOrder()).isEqualByComparingTo("50.00");

        Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(inventory.getQuantityReserved()).isZero();
    }

    @Test
    void creatingOrderForInactiveCustomerThrows() {
        Customer customer = createActiveCustomer();
        customerService.setActive(customer.getId(), false);
        Product product = createActiveProduct(10);

        assertThatThrownBy(() -> orderService.createOrder(orderFor(customer, product, 1)))
                .isInstanceOf(CustomerNotOrderableException.class);
    }

    @Test
    void creatingOrderForInactiveProductThrows() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(10);
        productService.setActive(product.getId(), false);

        assertThatThrownBy(() -> orderService.createOrder(orderFor(customer, product, 1)))
                .isInstanceOf(ProductNotOrderableException.class);
    }

    @Test
    void confirmOrderReservesStockAndTransitionsToConfirmed() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Order order = orderService.createOrder(orderFor(customer, product, 8));

        Order confirmed = orderService.confirmOrder(order.getId());

        assertThat(confirmed.getStatus()).isEqualTo("CONFIRMED");

        Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(inventory.getQuantityReserved()).isEqualTo(8);
        assertThat(inventory.getQuantityOnHand()).isEqualTo(20);
    }

    @Test
    void confirmingOrderWithInsufficientStockThrowsAndLeavesOrderPending() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(3);
        Order order = orderService.createOrder(orderFor(customer, product, 10));

        assertThatThrownBy(() -> orderService.confirmOrder(order.getId()))
                .isInstanceOf(com.joaodev.trackflowapi.inventory.service.InsufficientStockException.class);

        Order stillPending = orderService.findById(order.getId());
        assertThat(stillPending.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void confirmingAlreadyConfirmedOrderThrows() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Order order = orderService.createOrder(orderFor(customer, product, 2));
        orderService.confirmOrder(order.getId());

        assertThatThrownBy(() -> orderService.confirmOrder(order.getId()))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    void shipOrderFulfillsStockCreatesShipmentAndTransitionsToShipped() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Carrier carrier = createActiveCarrier();
        Order order = orderService.createOrder(orderFor(customer, product, 6));
        orderService.confirmOrder(order.getId());

        Order shipped = orderService.shipOrder(order.getId(), new ShipOrderRequest(carrier.getId()));

        assertThat(shipped.getStatus()).isEqualTo("SHIPPED");
        assertThat(shipped.getShipmentId()).isNotNull();

        Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(inventory.getQuantityOnHand()).isEqualTo(14);
        assertThat(inventory.getQuantityReserved()).isZero();

        Shipment shipment = shipmentService.findById(shipped.getShipmentId());
        assertThat(shipment.getOrigin()).isEqualTo(order.getOrigin());
        assertThat(shipment.getDestination()).isEqualTo(order.getDestination());
        assertThat(shipment.getCarrier()).isEqualTo(carrier.getName());
    }

    @Test
    void shippingWithInactiveCarrierThrows() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Carrier carrier = createActiveCarrier();
        carrierService.setActive(carrier.getId(), false);
        Order order = orderService.createOrder(orderFor(customer, product, 2));
        orderService.confirmOrder(order.getId());

        assertThatThrownBy(() -> orderService.shipOrder(order.getId(), new ShipOrderRequest(carrier.getId())))
                .isInstanceOf(CarrierNotOrderableException.class);
    }

    @Test
    void shippingUnconfirmedOrderThrows() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Carrier carrier = createActiveCarrier();
        Order order = orderService.createOrder(orderFor(customer, product, 2));

        assertThatThrownBy(() -> orderService.shipOrder(order.getId(), new ShipOrderRequest(carrier.getId())))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    void cancellingPendingOrderDoesNotTouchInventory() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Order order = orderService.createOrder(orderFor(customer, product, 5));

        Order cancelled = orderService.cancelOrder(order.getId());

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(inventory.getQuantityReserved()).isZero();
        assertThat(inventory.getQuantityOnHand()).isEqualTo(20);
    }

    @Test
    void cancellingConfirmedOrderReleasesReservedStock() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Order order = orderService.createOrder(orderFor(customer, product, 7));
        orderService.confirmOrder(order.getId());

        Order cancelled = orderService.cancelOrder(order.getId());

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(inventory.getQuantityReserved()).isZero();
        assertThat(inventory.getQuantityOnHand()).isEqualTo(20);
    }

    @Test
    void cancellingShippedOrderCancelsTheShipmentButDoesNotRestock() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Carrier carrier = createActiveCarrier();
        Order order = orderService.createOrder(orderFor(customer, product, 4));
        orderService.confirmOrder(order.getId());
        Order shipped = orderService.shipOrder(order.getId(), new ShipOrderRequest(carrier.getId()));

        Order cancelled = orderService.cancelOrder(shipped.getId());

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");

        Shipment shipment = shipmentService.findById(cancelled.getShipmentId());
        assertThat(shipment.getStatus()).isEqualTo("CANCELLED");

        Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(inventory.getQuantityOnHand()).isEqualTo(16);
    }

    @Test
    void cancellingDeliveredOrderThrows() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Carrier carrier = createActiveCarrier();
        Order order = orderService.createOrder(orderFor(customer, product, 2));
        orderService.confirmOrder(order.getId());
        Order shipped = orderService.shipOrder(order.getId(), new ShipOrderRequest(carrier.getId()));

        deliverShipmentAndWaitForOrder(shipped);

        assertThatThrownBy(() -> orderService.cancelOrder(shipped.getId()))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    void shipmentDeliveredEventuallyMarksOrderAsDelivered() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Carrier carrier = createActiveCarrier();
        Order order = orderService.createOrder(orderFor(customer, product, 3));
        orderService.confirmOrder(order.getId());
        Order shipped = orderService.shipOrder(order.getId(), new ShipOrderRequest(carrier.getId()));

        Order delivered = deliverShipmentAndWaitForOrder(shipped);

        assertThat(delivered.getStatus()).isEqualTo("DELIVERED");
    }

    private Order deliverShipmentAndWaitForOrder(Order shippedOrder) {
        Shipment shipment = shipmentService.findById(shippedOrder.getShipmentId());
        shipmentService.updateStatus(shipment.getTrackingCode(),
                new UpdateShipmentStatusRequest("DELIVERED", null, "Delivered to customer"));

        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            Order current = orderService.findById(shippedOrder.getId());
            if ("DELIVERED".equals(current.getStatus())) {
                return current;
            }
            sleep(50);
        }
        throw new AssertionError("Order " + shippedOrder.getOrderNumber() + " was not marked DELIVERED within 5s");
    }

    @Test
    void deleteOrderSoftDeletesAndExcludesFromFindAll() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(10);
        Order order = orderService.createOrder(orderFor(customer, product, 1));

        orderService.deleteOrder(order.getId());

        assertThat(orderService.findAll())
                .extracting(Order::getId)
                .doesNotContain(order.getId());

        Order stillFindable = orderService.findById(order.getId());
        assertThat(stillFindable.isDeleted()).isTrue();
    }

    @Test
    void findingNonExistentOrderThrows() {
        assertThatThrownBy(() -> orderService.findById(-1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void everyStatusTransitionPublishesOrderStatusChangedEvent() {
        Customer customer = createActiveCustomer();
        Product product = createActiveProduct(20);
        Order order = orderService.createOrder(orderFor(customer, product, 2));
        orderService.confirmOrder(order.getId());

        List<OrderStatusChangedEvent> published = applicationEvents
                .stream(OrderStatusChangedEvent.class)
                .filter(e -> e.orderId().equals(order.getId()))
                .toList();

        assertThat(published).extracting(OrderStatusChangedEvent::newStatus)
                .containsExactly("PENDING", "CONFIRMED");
        assertThat(published.get(1).previousStatus()).isEqualTo("PENDING");
    }
}