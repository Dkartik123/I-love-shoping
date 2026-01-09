package com.iloveshopping.service;

import com.iloveshopping.dto.response.CategoryResponse;
import com.iloveshopping.entity.Category;
import com.iloveshopping.exception.ResourceNotFoundException;
import com.iloveshopping.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for category management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Get all active categories.
     */
    @Cacheable(value = "categories")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrder().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get root categories with children.
     */
    @Cacheable(value = "categoryTree")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findRootCategories().stream()
                .map(category -> CategoryResponse.fromEntity(category, true, 0))
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryResponse.fromEntity(category, true, 0);
    }

    /**
     * Get category by slug.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return CategoryResponse.fromEntity(category, true, 0);
    }

    /**
     * Get subcategories of a category.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(UUID parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
