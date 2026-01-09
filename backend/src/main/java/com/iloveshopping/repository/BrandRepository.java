package com.iloveshopping.repository;

import com.iloveshopping.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Brand entity operations.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    List<Brand> findByIsActiveTrueOrderByName();
}
