package com.iloveshopping.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Product entity.
 * Tests data structure, validation, and business logic.
 */
class ProductTest {

    @Nested
    @DisplayName("Product Creation Tests")
    class ProductCreationTests {

        @Test
        @DisplayName("Should create product with all required fields")
        void shouldCreateProductWithRequiredFields() {
            // Given & When
            Product product = Product.builder()
                    .sku("TEST-001")
                    .name("Test Product")
                    .slug("test-product")
                    .price(new BigDecimal("99.99"))
                    .stockQuantity(100)
                    .build();

            // Then
            assertThat(product.getSku()).isEqualTo("TEST-001");
            assertThat(product.getName()).isEqualTo("Test Product");
            assertThat(product.getSlug()).isEqualTo("test-product");
            assertThat(product.getPrice()).isEqualByComparingTo("99.99");
            assertThat(product.getStockQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should have default values for optional fields")
        void shouldHaveDefaultValues() {
            // Given & When
            Product product = Product.builder()
                    .sku("TEST-001")
                    .name("Test Product")
                    .slug("test-product")
                    .price(new BigDecimal("99.99"))
                    .build();

            // Then
            assertThat(product.getStockQuantity()).isEqualTo(0);
            assertThat(product.getLowStockThreshold()).isEqualTo(10);
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getIsFeatured()).isFalse();
            assertThat(product.getIsDigital()).isFalse();
            assertThat(product.getAverageRating()).isEqualByComparingTo("0");
            assertThat(product.getReviewCount()).isEqualTo(0);
            assertThat(product.getViewCount()).isEqualTo(0);
            assertThat(product.getSoldCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create product with dimensions in both metric and imperial")
        void shouldCreateProductWithDimensions() {
            // Given & When
            Product product = Product.builder()
                    .sku("TEST-001")
                    .name("Test Product")
                    .slug("test-product")
                    .price(new BigDecimal("99.99"))
                    .weightKg(new BigDecimal("1.5"))
                    .weightLb(new BigDecimal("3.31"))
                    .lengthCm(new BigDecimal("30.0"))
                    .widthCm(new BigDecimal("20.0"))
                    .heightCm(new BigDecimal("10.0"))
                    .lengthIn(new BigDecimal("11.81"))
                    .widthIn(new BigDecimal("7.87"))
                    .heightIn(new BigDecimal("3.94"))
                    .build();

            // Then
            assertThat(product.getWeightKg()).isEqualByComparingTo("1.5");
            assertThat(product.getWeightLb()).isEqualByComparingTo("3.31");
            assertThat(product.getLengthCm()).isEqualByComparingTo("30.0");
            assertThat(product.getWidthCm()).isEqualByComparingTo("20.0");
            assertThat(product.getHeightCm()).isEqualByComparingTo("10.0");
        }
    }

    @Nested
    @DisplayName("Stock Management Tests")
    class StockManagementTests {

        @Test
        @DisplayName("Should report in stock when quantity > 0")
        void shouldBeInStockWhenQuantityPositive() {
            // Given
            Product product = createProductWithStock(10);

            // When & Then
            assertThat(product.isInStock()).isTrue();
        }

        @Test
        @DisplayName("Should report out of stock when quantity = 0")
        void shouldBeOutOfStockWhenQuantityZero() {
            // Given
            Product product = createProductWithStock(0);

            // When & Then
            assertThat(product.isInStock()).isFalse();
        }

        @Test
        @DisplayName("Should report low stock when quantity <= threshold")
        void shouldReportLowStock() {
            // Given
            Product product = Product.builder()
                    .sku("TEST-001")
                    .name("Test Product")
                    .slug("test-product")
                    .price(new BigDecimal("99.99"))
                    .stockQuantity(5)
                    .lowStockThreshold(10)
                    .build();

            // When & Then
            assertThat(product.isLowStock()).isTrue();
            assertThat(product.isInStock()).isTrue();
        }

        @Test
        @DisplayName("Should not report low stock when quantity > threshold")
        void shouldNotReportLowStockWhenAboveThreshold() {
            // Given
            Product product = Product.builder()
                    .sku("TEST-001")
                    .name("Test Product")
                    .slug("test-product")
                    .price(new BigDecimal("99.99"))
                    .stockQuantity(50)
                    .lowStockThreshold(10)
                    .build();

            // When & Then
            assertThat(product.isLowStock()).isFalse();
        }
    }

    @Nested
    @DisplayName("Pricing Tests")
    class PricingTests {

        @Test
        @DisplayName("Should detect product on sale")
        void shouldDetectOnSale() {
            // Given
            Product product = Product.builder()
                    .sku("TEST-001")
                    .name("Test Product")
                    .slug("test-product")
                    .price(new BigDecimal("79.99"))
                    .compareAtPrice(new BigDecimal("99.99"))
                    .build();

            // When & Then
            assertThat(product.isOnSale()).isTrue();
        }

        @Test
        @DisplayName("Should not be on sale when no compare price")
        void shouldNotBeOnSaleWithoutComparePrice() {
            // Given
            Product product = createProductWithStock(10);

            // When & Then
            assertThat(product.isOnSale()).isFalse();
        }

        @Test
        @DisplayName("Should calculate discount percentage correctly")
        void shouldCalculateDiscountPercentage() {
            // Given
            Product product = Product.builder()
                    .sku("TEST-001")
                    .name("Test Product")
                    .slug("test-product")
                    .price(new BigDecimal("75.00"))
                    .compareAtPrice(new BigDecimal("100.00"))
                    .build();

            // When
            BigDecimal discount = product.getDiscountPercentage();

            // Then
            assertThat(discount).isEqualByComparingTo("25.00");
        }

        @Test
        @DisplayName("Should return zero discount when not on sale")
        void shouldReturnZeroDiscountWhenNotOnSale() {
            // Given
            Product product = createProductWithStock(10);

            // When
            BigDecimal discount = product.getDiscountPercentage();

            // Then
            assertThat(discount).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("Image Management Tests")
    class ImageManagementTests {

        @Test
        @DisplayName("Should return primary image URL")
        void shouldReturnPrimaryImageUrl() {
            // Given
            Product product = createProductWithStock(10);
            ProductImage primaryImage = ProductImage.builder()
                    .imageUrl("https://example.com/primary.jpg")
                    .isPrimary(true)
                    .build();
            ProductImage secondaryImage = ProductImage.builder()
                    .imageUrl("https://example.com/secondary.jpg")
                    .isPrimary(false)
                    .build();
            
            product.setImages(new ArrayList<>());
            product.addImage(secondaryImage);
            product.addImage(primaryImage);

            // When
            String primaryUrl = product.getPrimaryImageUrl();

            // Then
            assertThat(primaryUrl).isEqualTo("https://example.com/primary.jpg");
        }

        @Test
        @DisplayName("Should return first image when no primary set")
        void shouldReturnFirstImageWhenNoPrimary() {
            // Given
            Product product = createProductWithStock(10);
            ProductImage image = ProductImage.builder()
                    .imageUrl("https://example.com/first.jpg")
                    .isPrimary(false)
                    .build();
            
            product.setImages(new ArrayList<>());
            product.addImage(image);

            // When
            String imageUrl = product.getPrimaryImageUrl();

            // Then
            assertThat(imageUrl).isEqualTo("https://example.com/first.jpg");
        }

        @Test
        @DisplayName("Should return null when no images")
        void shouldReturnNullWhenNoImages() {
            // Given
            Product product = createProductWithStock(10);
            product.setImages(new ArrayList<>());

            // When
            String imageUrl = product.getPrimaryImageUrl();

            // Then
            assertThat(imageUrl).isNull();
        }
    }

    @Nested
    @DisplayName("Rating Tests")
    class RatingTests {

        @Test
        @DisplayName("Should update rating correctly")
        void shouldUpdateRating() {
            // Given
            Product product = createProductWithStock(10);

            // When
            product.updateRating(new BigDecimal("4.5"), 100);

            // Then
            assertThat(product.getAverageRating()).isEqualByComparingTo("4.5");
            assertThat(product.getReviewCount()).isEqualTo(100);
        }
    }

    private Product createProductWithStock(int quantity) {
        return Product.builder()
                .sku("TEST-001")
                .name("Test Product")
                .slug("test-product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(quantity)
                .build();
    }
}
