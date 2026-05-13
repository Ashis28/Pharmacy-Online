package com.capg.pharma.orderservice.service;

import com.capg.pharma.orderservice.client.CatalogClient;
import com.capg.pharma.orderservice.client.PaymentClient;
import com.capg.pharma.orderservice.dto.MedicineDto;
import com.capg.pharma.orderservice.dto.OrderRequest;
import com.capg.pharma.orderservice.messaging.NotificationPublisher;
import com.capg.pharma.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for stock decrement logic in OrderService.
 * Verifies that decrementStockForOrder correctly calls CatalogClient
 * and handles failures gracefully.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceStockTest {

    @Mock OrderRepository orderRepo;
    @Mock CatalogClient catalogClient;
    @Mock PaymentClient paymentClient;
    @Mock NotificationPublisher notificationPublisher;

    @InjectMocks OrderService orderService;

    @Test
    void decrementStockForOrder_callsCatalogClientForEachItem() {
        OrderRequest.OrderItemRequest item1 = new OrderRequest.OrderItemRequest();
        item1.setMedicineId(1L);
        item1.setQuantity(2);

        OrderRequest.OrderItemRequest item2 = new OrderRequest.OrderItemRequest();
        item2.setMedicineId(2L);
        item2.setQuantity(3);

        doNothing().when(catalogClient).decrementStock(anyLong(), anyInt());

        orderService.decrementStockForOrder(List.of(item1, item2));

        verify(catalogClient).decrementStock(1L, 2);
        verify(catalogClient).decrementStock(2L, 3);
    }

    @Test
    void decrementStockForOrder_doesNotThrowWhenCatalogFails() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setMedicineId(99L);
        item.setQuantity(1);

        doThrow(new RuntimeException("Catalog service unavailable"))
                .when(catalogClient).decrementStock(99L, 1);

        // Should not propagate the exception — stock failure is non-fatal
        assertThatNoException().isThrownBy(
                () -> orderService.decrementStockForOrder(List.of(item))
        );
    }

    @Test
    void decrementStockForOrder_withEmptyList_doesNothing() {
        assertThatNoException().isThrownBy(
                () -> orderService.decrementStockForOrder(List.of())
        );
        verifyNoInteractions(catalogClient);
    }

    @Test
    void placeOrder_throwsWhenStockInsufficient() {
        MedicineDto medicine = new MedicineDto();
        medicine.setId(5L);
        medicine.setName("Vitamin C");
        medicine.setPrice(new BigDecimal("30.00"));
        medicine.setStockQuantity(1); // only 1 in stock

        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setMedicineId(5L);
        itemReq.setQuantity(5); // requesting 5

        OrderRequest req = new OrderRequest();
        req.setCustomerId(10L);
        req.setDeliveryAddress("123 Main St");
        req.setItems(List.of(itemReq));

        when(catalogClient.getMedicineById(5L)).thenReturn(medicine);

        assertThatThrownBy(() -> orderService.placeOrder(req, "customer@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("Vitamin C");
    }

    @Test
    void placeOrder_succeedsWhenStockSufficient() {
        MedicineDto medicine = new MedicineDto();
        medicine.setId(5L);
        medicine.setName("Paracetamol");
        medicine.setPrice(new BigDecimal("20.00"));
        medicine.setStockQuantity(100);

        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setMedicineId(5L);
        itemReq.setQuantity(2);

        OrderRequest req = new OrderRequest();
        req.setCustomerId(10L);
        req.setDeliveryAddress("123 Main St");
        req.setItems(List.of(itemReq));

        com.capg.pharma.orderservice.entity.Order savedOrder =
                new com.capg.pharma.orderservice.entity.Order();
        savedOrder.setId(1L);
        savedOrder.setCustomerId(10L);
        savedOrder.setCustomerEmail("customer@example.com");
        savedOrder.setStatus(com.capg.pharma.orderservice.entity.Order.OrderStatus.PENDING);
        savedOrder.setTotalAmount(new BigDecimal("40.00"));
        savedOrder.setDeliveryAddress("123 Main St");
        savedOrder.setItems(new java.util.ArrayList<>());

        when(catalogClient.getMedicineById(5L)).thenReturn(medicine);
        when(orderRepo.save(any())).thenReturn(savedOrder);

        assertThatNoException().isThrownBy(
                () -> orderService.placeOrder(req, "customer@example.com")
        );
    }
}
