package com.iloveshopping.controller;

import com.iloveshopping.dto.response.ApiResponse;
import com.iloveshopping.dto.response.ProductResponse;
import com.iloveshopping.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product catalog controller for browsing and searching products.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable UUID id) {
        
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(
            @PathVariable String slug) {
        
        ProductResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products with faceted filtering")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "Category ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Brand ID") @RequestParam(required = false) UUID brandId,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Minimum rating") @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "Only in stock") @RequestParam(required = false) Boolean inStock,
            @Parameter(description = "Only on sale") @RequestParam(required = false) Boolean onSale,
            @Parameter(description = "Sort by: price, rating, newest, bestselling, name") 
                @RequestParam(defaultValue = "relevance") String sortBy,
            @Parameter(description = "Sort direction: asc, desc") 
                @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.searchProducts(
                q, categoryId, brandId, minPrice, maxPrice, minRating,
                inStock, onSale, sortBy, sortDir, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get search suggestions based on prefix")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam String q) {
        
        List<String> suggestions = productService.getSearchSuggestions(q);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getFeaturedProducts(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/new-arrivals")
    @Operation(summary = "Get new arrivals")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getNewArrivals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getNewArrivals(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/on-sale")
    @Operation(summary = "Get products on sale")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getOnSaleProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getOnSaleProducts(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/best-sellers")
    @Operation(summary = "Get best selling products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getBestSellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getBestSellers(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/brand/{brandId}")
    @Operation(summary = "Get products by brand")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByBrand(
            @PathVariable UUID brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getProductsByBrand(brandId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get price range for filtering")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getPriceRange(
            @RequestParam(required = false) UUID categoryId) {
        
        Map<String, BigDecimal> priceRange = productService.getPriceRange(categoryId);
        return ResponseEntity.ok(ApiResponse.success(priceRange));
    }
}
