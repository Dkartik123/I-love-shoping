package com.iloveshopping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product attribute entity for defining filterable product attributes.
 */
@Entity
@Table(name = "product_attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type", length = 50)
    @Builder.Default
    private AttributeType attributeType = AttributeType.TEXT;

    @Column(name = "is_filterable")
    @Builder.Default
    private Boolean isFilterable = true;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum AttributeType {
        TEXT, NUMBER, BOOLEAN, COLOR, SIZE
    }
}
