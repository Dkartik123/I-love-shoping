package com.iloveshopping.controller;

import com.iloveshopping.dto.response.ProductResponse;
import com.iloveshopping.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Product API endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("Should get all products with pagination")
    void shouldGetAllProducts() throws Exception {
        // Given
        ProductResponse product = createSampleProduct();
        Page<ProductResponse> productPage = new PageImpl<>(List.of(product));
        
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.data.content[0].price").value(99.99));
    }

    @Test
    @DisplayName("Should get product by ID")
    void shouldGetProductById() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        ProductResponse product = createSampleProduct();
        product.setId(productId);
        
        when(productService.getProductById(productId)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    @DisplayName("Should get product by slug")
    void shouldGetProductBySlug() throws Exception {
        // Given
        ProductResponse product = createSampleProduct();
        
        when(productService.getProductBySlug("test-product")).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/products/slug/{slug}", "test-product")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("test-product"));
    }

    @Test
    @DisplayName("Should search products with filters")
    void shouldSearchProductsWithFilters() throws Exception {
        // Given
        ProductResponse product = createSampleProduct();
        Page<ProductResponse> productPage = new PageImpl<>(List.of(product));
        
        when(productService.searchProducts(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/products/search")
                        .param("q", "test")
                        .param("minPrice", "50")
                        .param("maxPrice", "150")
                        .param("inStock", "true")
                        .param("sortBy", "price")
                        .param("sortDir", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Should get search suggestions")
    void shouldGetSearchSuggestions() throws Exception {
        // Given
        when(productService.getSearchSuggestions("test"))
                .thenReturn(List.of("Test Product 1", "Test Product 2", "Test Gadget"));

        // When & Then
        mockMvc.perform(get("/products/suggestions")
                        .param("q", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("Test Product 1"));
    }

    @Test
    @DisplayName("Should get featured products")
    void shouldGetFeaturedProducts() throws Exception {
        // Given
        ProductResponse product = createSampleProduct();
        product.setFeatured(true);
        Page<ProductResponse> productPage = new PageImpl<>(List.of(product));
        
        when(productService.getFeaturedProducts(any(Pageable.class))).thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/products/featured")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].featured").value(true));
    }

    @Test
    @DisplayName("Should get price range")
    void shouldGetPriceRange() throws Exception {
        // Given
        when(productService.getPriceRange(null))
                .thenReturn(Map.of("min", new BigDecimal("10.00"), "max", new BigDecimal("1000.00")));

        // When & Then
        mockMvc.perform(get("/products/price-range")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.min").value(10.00))
                .andExpect(jsonPath("$.data.max").value(1000.00));
    }

    @Test
    @DisplayName("Should get products on sale")
    void shouldGetProductsOnSale() throws Exception {
        // Given
        ProductResponse product = createSampleProduct();
        product.setOnSale(true);
        product.setCompareAtPrice(new BigDecimal("149.99"));
        Page<ProductResponse> productPage = new PageImpl<>(List.of(product));
        
        when(productService.getOnSaleProducts(any(Pageable.class))).thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/products/on-sale")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].onSale").value(true));
    }

    private ProductResponse createSampleProduct() {
        return ProductResponse.builder()
                .id(UUID.randomUUID())
                .sku("TEST-001")
                .name("Test Product")
                .slug("test-product")
                .shortDescription("A test product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .inStock(true)
                .onSale(false)
                .averageRating(new BigDecimal("4.5"))
                .reviewCount(25)
                .featured(false)
                .active(true)
                .build();
    }
}
