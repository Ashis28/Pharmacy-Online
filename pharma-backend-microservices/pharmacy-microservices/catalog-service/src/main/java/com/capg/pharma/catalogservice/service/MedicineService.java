package com.capg.pharma.catalogservice.service;

import com.capg.pharma.catalogservice.dto.MedicineRequest;
import com.capg.pharma.catalogservice.dto.MedicineResponse;
import com.capg.pharma.catalogservice.entity.Category;
import com.capg.pharma.catalogservice.entity.Medicine;
import com.capg.pharma.catalogservice.exception.CategoryNotFoundException;
import com.capg.pharma.catalogservice.exception.MedicineNotFoundException;
import com.capg.pharma.catalogservice.repository.CategoryRepository;
import com.capg.pharma.catalogservice.repository.MedicineRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic service for medicine catalog operations.
 *
 * <p>Handles CRUD operations for medicines, name-based search, and
 * low-stock reporting. Category validation is performed on create/update.</p>
 */
@Service
public class MedicineService {

    private final MedicineRepository medicineRepo;
    private final CategoryRepository categoryRepo;

    /**
     * Constructs MedicineService with required repositories.
     *
     * @param medicineRepo  repository for medicine persistence
     * @param categoryRepo  repository for category lookups
     */
    public MedicineService(MedicineRepository medicineRepo, CategoryRepository categoryRepo) {
        this.medicineRepo = medicineRepo;
        this.categoryRepo = categoryRepo;
    }

    /**
     * Retrieves all medicines in the catalog.
     *
     * @return list of all medicines as response DTOs
     */
    public List<MedicineResponse> getAll() {
        return medicineRepo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Retrieves a single medicine by its ID.
     *
     * @param id the medicine's primary key
     * @return the medicine as a response DTO
     * @throws MedicineNotFoundException if no medicine exists with the given ID
     */
    public MedicineResponse getById(Long id) {
        return toResponse(medicineRepo.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + id)));
    }

    /**
     * Searches medicines by name (case-insensitive partial match).
     *
     * @param name the search term
     * @return list of matching medicines
     */
    public List<MedicineResponse> search(String name) {
        return medicineRepo.findByNameContainingIgnoreCase(name).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Creates a new medicine in the catalog.
     *
     * @param req the medicine creation request
     * @return the created medicine as a response DTO
     * @throws CategoryNotFoundException if the specified categoryId does not exist
     */
    public MedicineResponse create(MedicineRequest req) {
        Medicine m = new Medicine();
        mapFields(m, req);
        return toResponse(medicineRepo.save(m));
    }

    /**
     * Updates an existing medicine's details.
     *
     * @param id  the medicine's primary key
     * @param req the update request
     * @return the updated medicine as a response DTO
     * @throws MedicineNotFoundException if no medicine exists with the given ID
     * @throws CategoryNotFoundException if the specified categoryId does not exist
     */
    public MedicineResponse update(Long id, MedicineRequest req) {
        Medicine m = medicineRepo.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with id: " + id));
        mapFields(m, req);
        return toResponse(medicineRepo.save(m));
    }

    /**
     * Deletes a medicine from the catalog.
     *
     * @param id the medicine's primary key
     * @throws MedicineNotFoundException if no medicine exists with the given ID
     */
    public void delete(Long id) {
        if (!medicineRepo.existsById(id)) {
            throw new MedicineNotFoundException("Medicine not found with id: " + id);
        }
        medicineRepo.deleteById(id);
    }

    /**
     * Decrements the stock quantity of a medicine by the given amount.
     * Called internally by order-service after an order is placed.
     *
     * @param id       the medicine's primary key
     * @param quantity the quantity to deduct (must be positive)
     * @throws MedicineNotFoundException    if no medicine exists with the given ID
     * @throws IllegalArgumentException     if quantity exceeds available stock
     */
    @org.springframework.transaction.annotation.Transactional
    public void decrementStock(Long id, int quantity) {
        if (!medicineRepo.existsById(id)) {
            throw new MedicineNotFoundException("Medicine not found with id: " + id);
        }
        int updated = medicineRepo.decrementStock(id, quantity);
        if (updated == 0) {
            // Row wasn't updated — stock was insufficient
            MedicineResponse m = getById(id);
            throw new IllegalArgumentException(
                    "Insufficient stock for '" + m.getName() +
                    "'. Available: " + m.getStockQuantity() + ", requested: " + quantity);
        }
    }

    /**
     * Returns the count of medicines with stock at or below 10 units.
     *
     * @return count of low-stock medicines
     */
    public long getLowStockCount() {
        return medicineRepo.countLowStock();
    }

    /**
     * Returns all medicines with stock at or below 10 units.
     *
     * @return list of low-stock medicines
     */
    public List<MedicineResponse> getLowStockMedicines() {
        return medicineRepo.findLowStockMedicines().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Maps fields from a request DTO onto a Medicine entity.
     * Resolves the category by ID if provided.
     *
     * @param m   the medicine entity to populate
     * @param req the source request DTO
     * @throws CategoryNotFoundException if the categoryId is provided but not found
     */
    private void mapFields(Medicine m, MedicineRequest req) {
        m.setName(req.getName());
        m.setDescription(req.getDescription());
        m.setPrice(req.getPrice());
        m.setStockQuantity(req.getStockQuantity());
        m.setRequiresPrescription(req.isRequiresPrescription());
        m.setExpiryDate(req.getExpiryDate());
        if (req.getCategoryId() != null) {
            Category cat = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + req.getCategoryId()));
            m.setCategory(cat);
        }
    }

    /**
     * Converts a Medicine entity to a MedicineResponse DTO.
     *
     * @param m the medicine entity
     * @return the response DTO
     */
    private MedicineResponse toResponse(Medicine m) {
        MedicineResponse r = new MedicineResponse();
        r.setId(m.getId());
        r.setName(m.getName());
        r.setDescription(m.getDescription());
        r.setPrice(m.getPrice());
        r.setStockQuantity(m.getStockQuantity());
        r.setRequiresPrescription(m.isRequiresPrescription());
        r.setExpiryDate(m.getExpiryDate());
        if (m.getCategory() != null) r.setCategoryName(m.getCategory().getName());
        return r;
    }
}
