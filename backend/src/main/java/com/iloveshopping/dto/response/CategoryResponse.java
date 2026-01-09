package com.iloveshopping.dto.response;

import com.iloveshopping.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Category response DTO with optional children.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private UUID parentId;
    private List<CategoryResponse> children;
    private long productCount;

    public static CategoryResponse fromEntity(Category category) {
        return fromEntity(category, false, 0);
    }

    public static CategoryResponse fromEntity(Category category, boolean includeChildren, long productCount) {
        CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .displayOrder(category.getDisplayOrder())
                .productCount(productCount);

        if (category.getParent() != null) {
            builder.parentId(category.getParent().getId());
        }

        if (includeChildren && category.getChildren() != null) {
            builder.children(category.getChildren().stream()
                    .filter(Category::getIsActive)
                    .map(child -> fromEntity(child, true, 0))
                    .collect(Collectors.toList()));
        } else {
            builder.children(new ArrayList<>());
        }

        return builder.build();
    }
}
