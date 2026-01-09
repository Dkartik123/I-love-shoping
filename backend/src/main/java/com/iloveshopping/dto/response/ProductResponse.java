package com.iloveshopping.dto.response;

import com.iloveshopping.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Product response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;
    private String sku;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer stockQuantity;
    private boolean inStock;
    private boolean lowStock;
    private boolean onSale;
    private BigDecimal discountPercentage;
    
    // Dimensions
    private BigDecimal weightKg;
    private BigDecimal weightLb;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private BigDecimal lengthIn;
    private BigDecimal widthIn;
    private BigDecimal heightIn;
    
    // Relations
    private CategoryInfo category;
    private BrandInfo brand;
    private List<ImageInfo> images;
    private List<AttributeInfo> attributes;
    private List<String> tags;
    
    // Ratings
    private BigDecimal averageRating;
    private Integer reviewCount;
    
    // Flags
    private boolean active;
    private boolean featured;
    private boolean digital;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private UUID id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandInfo {
        private UUID id;
        private String name;
        private String slug;
        private String logoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private UUID id;
        private String imageUrl;
        private String altText;
        private boolean primary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeInfo {
        private String name;
        private String displayName;
        private String value;
    }

    public static ProductResponse fromEntity(Product product) {
        ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .stockQuantity(product.getStockQuantity())
                .inStock(product.isInStock())
                .lowStock(product.isLowStock())
                .onSale(product.isOnSale())
                .discountPercentage(product.getDiscountPercentage())
                .weightKg(product.getWeightKg())
                .weightLb(product.getWeightLb())
                .lengthCm(product.getLengthCm())
                .widthCm(product.getWidthCm())
                .heightCm(product.getHeightCm())
                .lengthIn(product.getLengthIn())
                .widthIn(product.getWidthIn())
                .heightIn(product.getHeightIn())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .active(product.getIsActive())
                .featured(product.getIsFeatured())
                .digital(product.getIsDigital())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            builder.category(CategoryInfo.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .slug(product.getCategory().getSlug())
                    .build());
        }

        if (product.getBrand() != null) {
            builder.brand(BrandInfo.builder()
                    .id(product.getBrand().getId())
                    .name(product.getBrand().getName())
                    .slug(product.getBrand().getSlug())
                    .logoUrl(product.getBrand().getLogoUrl())
                    .build());
        }

        if (product.getImages() != null) {
            builder.images(product.getImages().stream()
                    .map(img -> ImageInfo.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .primary(img.getIsPrimary())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (product.getAttributeValues() != null) {
            builder.attributes(product.getAttributeValues().stream()
                    .map(av -> AttributeInfo.builder()
                            .name(av.getAttribute().getName())
                            .displayName(av.getAttribute().getDisplayName())
                            .value(av.getValue())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (product.getTags() != null) {
            builder.tags(product.getTags().stream()
                    .map(tag -> tag.getName())
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    public static ProductResponse summary(Product product) {
        ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .slug(product.getSlug())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .inStock(product.isInStock())
                .onSale(product.isOnSale())
                .discountPercentage(product.getDiscountPercentage())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .featured(product.getIsFeatured());

        // Get primary image only
        String primaryImage = product.getPrimaryImageUrl();
        if (primaryImage != null) {
            builder.images(List.of(ImageInfo.builder()
                    .imageUrl(primaryImage)
                    .primary(true)
                    .build()));
        }

        if (product.getBrand() != null) {
            builder.brand(BrandInfo.builder()
                    .id(product.getBrand().getId())
                    .name(product.getBrand().getName())
                    .slug(product.getBrand().getSlug())
                    .build());
        }

        return builder.build();
    }
}
