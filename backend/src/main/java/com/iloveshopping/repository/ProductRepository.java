package com.iloveshopping.repository;

import com.iloveshopping.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Product entity with advanced search capabilities.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySlug(String slug);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(UUID categoryId, Pageable pageable);

    Page<Product> findByBrandIdAndIsActiveTrue(UUID brandId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isFeatured = true")
    Page<Product> findFeaturedProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Page<Product> findNewArrivals(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.compareAtPrice IS NOT NULL AND p.compareAtPrice > p.price")
    Page<Product> findOnSaleProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.soldCount DESC")
    Page<Product> findBestSellers(Pageable pageable);

    // Full text search using PostgreSQL
    @Query(value = "SELECT * FROM products p WHERE p.is_active = true AND " +
            "to_tsvector('english', p.name || ' ' || COALESCE(p.description, '')) @@ plainto_tsquery('english', :query)",
            countQuery = "SELECT COUNT(*) FROM products p WHERE p.is_active = true AND " +
            "to_tsvector('english', p.name || ' ' || COALESCE(p.description, '')) @@ plainto_tsquery('english', :query)",
            nativeQuery = true)
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    // Search suggestions
    @Query(value = "SELECT DISTINCT p.name FROM products p WHERE p.is_active = true AND LOWER(p.name) LIKE LOWER(CONCAT(:prefix, '%')) LIMIT 10",
            nativeQuery = true)
    List<String> findSearchSuggestions(@Param("prefix") String prefix);

    // Price range
    @Query("SELECT MIN(p.price), MAX(p.price) FROM Product p WHERE p.isActive = true")
    Object[] findPriceRange();

    @Query("SELECT MIN(p.price), MAX(p.price) FROM Product p WHERE p.isActive = true AND p.category.id = :categoryId")
    Object[] findPriceRangeByCategory(@Param("categoryId") UUID categoryId);

    // Stock management
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int decreaseStock(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :productId")
    void increaseStock(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") UUID productId);

    // Count by category
    @Query("SELECT p.category.id, COUNT(p) FROM Product p WHERE p.isActive = true GROUP BY p.category.id")
    List<Object[]> countByCategory();

    // Count by brand
    @Query("SELECT p.brand.id, COUNT(p) FROM Product p WHERE p.isActive = true GROUP BY p.brand.id")
    List<Object[]> countByBrand();
}
