package com.iloveshopping.controller;

import com.iloveshopping.dto.response.ApiResponse;
import com.iloveshopping.dto.response.CategoryResponse;
import com.iloveshopping.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Category controller for browsing product categories.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree with children")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
        List<CategoryResponse> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(categoryTree));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable UUID id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(
            @PathVariable String slug) {
        CategoryResponse category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping("/{parentId}/subcategories")
    @Operation(summary = "Get subcategories of a category")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubcategories(
            @PathVariable UUID parentId) {
        List<CategoryResponse> subcategories = categoryService.getSubcategories(parentId);
        return ResponseEntity.ok(ApiResponse.success(subcategories));
    }
}
