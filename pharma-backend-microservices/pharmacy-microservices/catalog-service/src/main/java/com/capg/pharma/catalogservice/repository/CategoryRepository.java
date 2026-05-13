package com.capg.pharma.catalogservice.repository;

import com.capg.pharma.catalogservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Category} entities.
 * Provides standard CRUD operations for medicine categories.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {}
