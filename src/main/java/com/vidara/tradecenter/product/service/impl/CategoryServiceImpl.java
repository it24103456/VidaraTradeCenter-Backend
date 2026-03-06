package com.vidara.tradecenter.product.service.impl;

import com.vidara.tradecenter.common.exception.DuplicateResourceException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.common.util.SlugUtils;
import com.vidara.tradecenter.product.dto.request.CategoryRequest;
import com.vidara.tradecenter.product.dto.response.CategoryResponse;
import com.vidara.tradecenter.product.mapper.CategoryMapper;
import com.vidara.tradecenter.product.model.Category;
import com.vidara.tradecenter.product.repository.CategoryRepository;
import com.vidara.tradecenter.product.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }


    // CREATE CATEGORY

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        // Check for duplicate name
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        Category category = categoryMapper.toCategory(request);

        // Auto-generate slug from name
        String slug = SlugUtils.toSlug(request.getName());

        // Ensure slug uniqueness
        int counter = 1;
        String originalSlug = slug;
        while (categoryRepository.existsBySlug(slug)) {
            slug = SlugUtils.makeUnique(originalSlug, counter++);
        }
        category.setSlug(slug);

        // Set parent category if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParentCategory(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        logger.info("Category created: {} (slug: {})", savedCategory.getName(), savedCategory.getSlug());

        return categoryMapper.toCategoryResponse(savedCategory);
    }


    // GET ALL CATEGORIES

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }


    // GET CATEGORY BY ID

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return categoryMapper.toCategoryResponse(category);
    }


    // UPDATE CATEGORY

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check for duplicate name (exclude current category)
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        // Update fields
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());

        // Re-generate slug if name changed
        if (!category.getSlug().equals(SlugUtils.toSlug(request.getName()))) {
            String slug = SlugUtils.toSlug(request.getName());
            int counter = 1;
            String originalSlug = slug;
            while (categoryRepository.existsBySlug(slug)) {
                slug = SlugUtils.makeUnique(originalSlug, counter++);
            }
            category.setSlug(slug);
        }

        // Update parent category
        if (request.getParentId() != null) {
            // Prevent setting self as parent
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        logger.info("Category updated: {} (id: {})", updatedCategory.getName(), updatedCategory.getId());

        return categoryMapper.toCategoryResponse(updatedCategory);
    }


    // DELETE CATEGORY

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        categoryRepository.delete(category);
        logger.info("Category deleted: {} (id: {})", category.getName(), id);
    }
}
