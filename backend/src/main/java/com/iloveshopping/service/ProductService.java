package com.iloveshopping.service;

import com.iloveshopping.dto.response.ProductResponse;
import com.iloveshopping.entity.Product;
import com.iloveshopping.exception.ResourceNotFoundException;
import com.iloveshopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for product management and search functionality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Get all active products with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable)
                .map(ProductResponse::summary);
    }

    /**
     * Get product by ID.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        // Increment view count asynchronously
        productRepository.incrementViewCount(id);
        
        return ProductResponse.fromEntity(product);
    }

    /**
     * Get product by slug.
     */
    @Transactional
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        
        productRepository.incrementViewCount(product.getId());
        
        return ProductResponse.fromEntity(product);
    }

    /**
     * Search products with faceted filtering.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String query,
            UUID categoryId,
            UUID brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minRating,
            Boolean inStock,
            Boolean onSale,
            String sortBy,
            String sortDir,
            Pageable pageable) {

        // Build specification for filtering
        Specification<Product> spec = Specification.where(isActive());

        if (query != null && !query.isBlank()) {
            spec = spec.and(searchByKeyword(query));
        }

        if (categoryId != null) {
            spec = spec.and(hasCategory(categoryId));
        }

        if (brandId != null) {
            spec = spec.and(hasBrand(brandId));
        }

        if (minPrice != null) {
            spec = spec.and(priceGreaterThanOrEqual(minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and(priceLessThanOrEqual(maxPrice));
        }

        if (minRating != null) {
            spec = spec.and(ratingGreaterThanOrEqual(minRating));
        }

        if (Boolean.TRUE.equals(inStock)) {
            spec = spec.and(inStock());
        }

        if (Boolean.TRUE.equals(onSale)) {
            spec = spec.and(onSale());
        }

        // Build sort
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return productRepository.findAll(spec, pageableWithSort)
                .map(ProductResponse::summary);
    }

    /**
     * Get search suggestions based on prefix.
     */
    @Cacheable(value = "searchSuggestions", key = "#prefix")
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String prefix) {
        if (prefix == null || prefix.length() < 2) {
            return List.of();
        }
        return productRepository.findSearchSuggestions(prefix);
    }

    /**
     * Get featured products.
     */
    @Cacheable(value = "featuredProducts")
    @Transactional(readOnly = true)
    public Page<ProductResponse> getFeaturedProducts(Pageable pageable) {
        return productRepository.findFeaturedProducts(pageable)
                .map(ProductResponse::summary);
    }

    /**
     * Get new arrivals.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getNewArrivals(Pageable pageable) {
        return productRepository.findNewArrivals(pageable)
                .map(ProductResponse::summary);
    }

    /**
     * Get products on sale.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getOnSaleProducts(Pageable pageable) {
        return productRepository.findOnSaleProducts(pageable)
                .map(ProductResponse::summary);
    }

    /**
     * Get best sellers.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getBestSellers(Pageable pageable) {
        return productRepository.findBestSellers(pageable)
                .map(ProductResponse::summary);
    }

    /**
     * Get products by category.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(UUID categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
                .map(ProductResponse::summary);
    }

    /**
     * Get products by brand.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByBrand(UUID brandId, Pageable pageable) {
        return productRepository.findByBrandIdAndIsActiveTrue(brandId, pageable)
                .map(ProductResponse::summary);
    }

    /**
     * Get price range for filtering.
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getPriceRange(UUID categoryId) {
        Object[] result;
        if (categoryId != null) {
            result = productRepository.findPriceRangeByCategory(categoryId);
        } else {
            result = productRepository.findPriceRange();
        }

        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;
        
        if (result != null && result.length >= 2) {
            if (result[0] != null) {
                min = result[0] instanceof BigDecimal ? (BigDecimal) result[0] : new BigDecimal(result[0].toString());
            }
            if (result[1] != null) {
                max = result[1] instanceof BigDecimal ? (BigDecimal) result[1] : new BigDecimal(result[1].toString());
            }
        }

        return Map.of("min", min, "max", max);
    }

    // Specification builders
    private Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    private Specification<Product> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("sku")), pattern)
            );
        };
    }

    private Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    private Specification<Product> hasBrand(UUID brandId) {
        return (root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId);
    }

    private Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    private Specification<Product> ratingGreaterThanOrEqual(BigDecimal minRating) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("averageRating"), minRating);
    }

    private Specification<Product> inStock() {
        return (root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0);
    }

    private Specification<Product> onSale() {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("compareAtPrice")),
                cb.greaterThan(root.get("compareAtPrice"), root.get("price"))
        );
    }

    private Sort buildSort(String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return switch (sortBy != null ? sortBy.toLowerCase() : "relevance") {
            case "price" -> Sort.by(direction, "price");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "bestselling" -> Sort.by(Sort.Direction.DESC, "soldCount");
            case "name" -> Sort.by(direction, "name");
            default -> Sort.by(Sort.Direction.DESC, "soldCount", "averageRating");
        };
    }
}
