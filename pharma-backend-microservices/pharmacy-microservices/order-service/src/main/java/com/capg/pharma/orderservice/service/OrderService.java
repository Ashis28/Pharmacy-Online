package com.capg.pharma.orderservice.service;

import com.capg.pharma.orderservice.client.CatalogClient;
import com.capg.pharma.orderservice.client.PaymentClient;
import com.capg.pharma.orderservice.messaging.NotificationPublisher;
import com.capg.pharma.orderservice.dto.*;
import com.capg.pharma.orderservice.entity.Order;
import com.capg.pharma.orderservice.entity.OrderItem;
import com.capg.pharma.orderservice.exception.InvalidOrderStatusException;
import com.capg.pharma.orderservice.exception.OrderNotFoundException;
import com.capg.pharma.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic service for order management.
 *
 * <p>Handles order placement (with medicine price lookup via Feign),
 * status updates, and revenue reporting. Publishes ORDER_PLACED events
 * to RabbitMQ after successful order creation.</p>
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepo;
    private final CatalogClient catalogClient;
    private final PaymentClient paymentClient;
    private final NotificationPublisher notificationPublisher;

    /**
     * Constructs OrderService with required dependencies.
     *
     * @param orderRepo            repository for order persistence
     * @param catalogClient        Feign client for medicine price lookup
     * @param paymentClient        Feign client for payment processing
     * @param notificationPublisher RabbitMQ publisher for order events
     */
    public OrderService(OrderRepository orderRepo, CatalogClient catalogClient,
                        PaymentClient paymentClient, NotificationPublisher notificationPublisher) {
        this.orderRepo = orderRepo;
        this.catalogClient = catalogClient;
        this.paymentClient = paymentClient;
        this.notificationPublisher = notificationPublisher;
    }

    /**
     * Places a new order for a customer.
     * Stock decrement is done AFTER the transaction commits to avoid
     * holding the transaction open during cross-service Feign calls.
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest req, String customerEmail) {
        Order order = new Order();
        order.setCustomerId(req.getCustomerId());
        order.setCustomerEmail(customerEmail);
        order.setDeliveryAddress(req.getDeliveryAddress());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            MedicineDto medicine = catalogClient.getMedicineById(itemReq.getMedicineId());

            // Validate stock before building the item
            if (medicine.getStockQuantity() != null &&
                    medicine.getStockQuantity() < itemReq.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for '" + medicine.getName() +
                        "'. Available: " + medicine.getStockQuantity() +
                        ", requested: " + itemReq.getQuantity());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMedicineId(medicine.getId());
            item.setMedicineName(medicine.getName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(medicine.getPrice());
            order.getItems().add(item);
            total = total.add(medicine.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }
        order.setTotalAmount(total);
        Order saved = orderRepo.save(order);

        // Publish notification event — failure is non-critical
        try {
            notificationPublisher.publish(new NotificationRequest(
                    customerEmail,
                    "Order Placed - #" + saved.getId(),
                    "Your order #" + saved.getId() + " has been placed. Total: Rs." + saved.getTotalAmount(),
                    "ORDER_PLACED"
            ));
        } catch (Exception ignored) { /* non-critical */ }

        return toResponse(saved);
    }

    /**
     * Decrements stock for all items in an order.
     * Called AFTER placeOrder transaction commits — runs outside any transaction
     * so the Feign HTTP call to catalog-service is not wrapped in a JPA transaction.
     */
    public void decrementStockForOrder(List<OrderRequest.OrderItemRequest> items) {
        for (OrderRequest.OrderItemRequest itemReq : items) {
            try {
                catalogClient.decrementStock(itemReq.getMedicineId(), itemReq.getQuantity());
                log.info("[STOCK] Decremented {} units for medicine id={}",
                        itemReq.getQuantity(), itemReq.getMedicineId());
            } catch (Exception e) {
                log.error("[STOCK] Failed to decrement stock for medicine id={} qty={}: {}",
                        itemReq.getMedicineId(), itemReq.getQuantity(), e.getMessage());
            }
        }
    }

    /**
     * Retrieves a single order by its ID.
     *
     * @param id the order's primary key
     * @return the order as a response DTO
     * @throws OrderNotFoundException if no order exists with the given ID
     */
    public OrderResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Retrieves all orders placed by a specific customer.
     *
     * @param customerId the customer's ID
     * @return list of orders for that customer
     */
    public List<OrderResponse> getByCustomer(Long customerId) {
        return orderRepo.findByCustomerId(customerId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Retrieves all orders in the system. Admin use only.
     *
     * @return list of all orders
     */
    public List<OrderResponse> getAll() {
        return orderRepo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Updates the status of an order.
     *
     * @param id     the order's primary key
     * @param status the new status string (must match an {@link Order.OrderStatus} enum value)
     * @return the updated order as a response DTO
     * @throws OrderNotFoundException      if no order exists with the given ID
     * @throws InvalidOrderStatusException if the status string is not a valid enum value
     */
    public OrderResponse updateStatus(Long id, String status) {
        Order order = findOrThrow(id);
        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException("Invalid order status: " + status +
                    ". Valid values: PENDING, PAID, PACKED, SHIPPED, DELIVERED, CANCELLED");
        }
        return toResponse(orderRepo.save(order));
    }

    /**
     * Returns the total count of all orders.
     *
     * @return total order count
     */
    public long getCount() {
        return orderRepo.count();
    }

    /**
     * Calculates total revenue from DELIVERED orders within a date range.
     *
     * @param from start date string (ISO format: yyyy-MM-dd)
     * @param to   end date string (ISO format: yyyy-MM-dd)
     * @return total revenue as BigDecimal
     */
    public BigDecimal getRevenueBetween(String from, String to) {
        LocalDateTime fromDt = LocalDate.parse(from).atStartOfDay();
        LocalDateTime toDt = LocalDate.parse(to).atTime(23, 59, 59);
        return orderRepo.sumRevenueBetween(fromDt, toDt);
    }

    /**
     * Finds an order by ID or throws if not found.
     *
     * @param id the order's primary key
     * @return the order entity
     * @throws OrderNotFoundException if not found
     */
    private Order findOrThrow(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }

    /**
     * Converts an Order entity to an OrderResponse DTO.
     *
     * @param o the order entity
     * @return the response DTO with all fields and items
     */
    private OrderResponse toResponse(Order o) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setCustomerId(o.getCustomerId());
        r.setCustomerEmail(o.getCustomerEmail());
        r.setStatus(o.getStatus().name());
        r.setTotalAmount(o.getTotalAmount());
        r.setDeliveryAddress(o.getDeliveryAddress());
        r.setCreatedAt(o.getCreatedAt());
        r.setItems(o.getItems().stream().map(i -> {
            OrderResponse.ItemDto d = new OrderResponse.ItemDto();
            d.setMedicineId(i.getMedicineId());
            d.setMedicineName(i.getMedicineName());
            d.setQuantity(i.getQuantity());
            d.setUnitPrice(i.getUnitPrice());
            return d;
        }).collect(Collectors.toList()));
        return r;
    }
}
