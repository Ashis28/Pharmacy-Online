package com.capg.pharma.orderservice.service;

import com.capg.pharma.orderservice.client.CatalogClient;
import com.capg.pharma.orderservice.client.PaymentClient;
import com.capg.pharma.orderservice.dto.MedicineDto;
import com.capg.pharma.orderservice.dto.NotificationRequest;
import com.capg.pharma.orderservice.dto.OrderRequest;
import com.capg.pharma.orderservice.dto.OrderResponse;
import com.capg.pharma.orderservice.entity.Order;
import com.capg.pharma.orderservice.exception.InvalidOrderStatusException;
import com.capg.pharma.orderservice.exception.OrderNotFoundException;
import com.capg.pharma.orderservice.messaging.NotificationPublisher;
import com.capg.pharma.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepo;
    @Mock CatalogClient catalogClient;
    @Mock PaymentClient paymentClient;
    @Mock NotificationPublisher notificationPublisher;

    @InjectMocks OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setCustomerId(10L);
        order.setCustomerEmail("customer@example.com");
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setDeliveryAddress("123 Main St");
        order.setItems(new ArrayList<>());
    }

    @Test
    void getById_returnsOrder() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        OrderResponse result = orderService.getById(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_returnsAllOrders() {
        when(orderRepo.findAll()).thenReturn(List.of(order));
        List<OrderResponse> result = orderService.getAll();
        assertThat(result).hasSize(1);
    }

    @Test
    void getByCustomer_returnsCustomerOrders() {
        when(orderRepo.findByCustomerId(10L)).thenReturn(List.of(order));
        List<OrderResponse> result = orderService.getByCustomer(10L);
        assertThat(result).hasSize(1);
    }

    @Test
    void updateStatus_updatesSuccessfully() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        order.setStatus(Order.OrderStatus.PAID);
        when(orderRepo.save(any(Order.class))).thenReturn(order);

        OrderResponse result = orderService.updateStatus(1L, "PAID");
        assertThat(result.getStatus()).isEqualTo("PAID");
    }

    @Test
    void updateStatus_throwsWhenOrderNotFound() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.updateStatus(99L, "PAID"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updateStatus_throwsWhenInvalidStatus() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        assertThatThrownBy(() -> orderService.updateStatus(1L, "INVALID_STATUS"))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("INVALID_STATUS");
    }

    @Test
    void placeOrder_createsOrderSuccessfully() {
        MedicineDto medicine = new MedicineDto();
        medicine.setId(5L);
        medicine.setName("Paracetamol");
        medicine.setPrice(new BigDecimal("20.00"));

        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setMedicineId(5L);
        itemReq.setQuantity(2);

        OrderRequest req = new OrderRequest();
        req.setCustomerId(10L);
        req.setDeliveryAddress("123 Main St");
        req.setItems(List.of(itemReq));

        when(catalogClient.getMedicineById(5L)).thenReturn(medicine);
        when(orderRepo.save(any(Order.class))).thenReturn(order);
        doNothing().when(notificationPublisher).publish(any(NotificationRequest.class));

        OrderResponse result = orderService.placeOrder(req, "customer@example.com");
        assertThat(result).isNotNull();
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    void getCount_returnsCount() {
        when(orderRepo.count()).thenReturn(5L);
        assertThat(orderService.getCount()).isEqualTo(5L);
    }
}
