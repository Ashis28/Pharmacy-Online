package com.capg.pharma.catalogservice.service;

import com.capg.pharma.catalogservice.entity.Category;
import com.capg.pharma.catalogservice.entity.Medicine;
import com.capg.pharma.catalogservice.exception.MedicineNotFoundException;
import com.capg.pharma.catalogservice.repository.CategoryRepository;
import com.capg.pharma.catalogservice.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the stock decrement logic — the core of the stock-not-decreasing bug fix.
 */
@ExtendWith(MockitoExtension.class)
class MedicineServiceStockTest {

    @Mock MedicineRepository medicineRepo;
    @Mock CategoryRepository categoryRepo;

    @InjectMocks MedicineService medicineService;

    private Medicine medicine;

    @BeforeEach
    void setUp() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Vitamins");

        medicine = new Medicine();
        medicine.setId(1L);
        medicine.setName("Vitamin D3");
        medicine.setPrice(new BigDecimal("95.00"));
        medicine.setStockQuantity(10);
        medicine.setCategory(cat);
    }

    @Test
    void decrementStock_successfullyDecrementsWhenStockSufficient() {
        when(medicineRepo.existsById(1L)).thenReturn(true);
        when(medicineRepo.decrementStock(1L, 3)).thenReturn(1); // 1 row updated

        assertThatNoException().isThrownBy(() -> medicineService.decrementStock(1L, 3));
        verify(medicineRepo).decrementStock(1L, 3);
    }

    @Test
    void decrementStock_throwsWhenStockInsufficient() {
        when(medicineRepo.existsById(1L)).thenReturn(true);
        when(medicineRepo.decrementStock(1L, 20)).thenReturn(0); // 0 rows — stock too low
        when(medicineRepo.findById(1L)).thenReturn(Optional.of(medicine));

        assertThatThrownBy(() -> medicineService.decrementStock(1L, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void decrementStock_throwsWhenMedicineNotFound() {
        when(medicineRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> medicineService.decrementStock(99L, 1))
                .isInstanceOf(MedicineNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void decrementStock_exactStockAmount_succeeds() {
        // Ordering exactly the available stock should work
        when(medicineRepo.existsById(1L)).thenReturn(true);
        when(medicineRepo.decrementStock(1L, 10)).thenReturn(1);

        assertThatNoException().isThrownBy(() -> medicineService.decrementStock(1L, 10));
    }

    @Test
    void decrementStock_zeroStock_throwsInsufficient() {
        medicine.setStockQuantity(0);
        when(medicineRepo.existsById(1L)).thenReturn(true);
        when(medicineRepo.decrementStock(1L, 1)).thenReturn(0);
        when(medicineRepo.findById(1L)).thenReturn(Optional.of(medicine));

        assertThatThrownBy(() -> medicineService.decrementStock(1L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }
}
