package com.vidara.tradecenter.product.service.impl;

import com.vidara.tradecenter.common.exception.DuplicateResourceException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.common.util.SlugUtils;
import com.vidara.tradecenter.product.dto.request.BrandRequest;
import com.vidara.tradecenter.product.dto.response.BrandResponse;
import com.vidara.tradecenter.product.mapper.BrandMapper;
import com.vidara.tradecenter.product.model.Brand;
import com.vidara.tradecenter.product.repository.BrandRepository;
import com.vidara.tradecenter.product.service.BrandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {

    private static final Logger logger = LoggerFactory.getLogger(BrandServiceImpl.class);

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public BrandServiceImpl(BrandRepository brandRepository,
                            BrandMapper brandMapper) {
        this.brandRepository = brandRepository;
        this.brandMapper = brandMapper;
    }


    // CREATE BRAND

    @Override
    @Transactional
    public BrandResponse create(BrandRequest request) {
        // Check for duplicate name
        if (brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Brand", "name", request.getName());
        }

        Brand brand = brandMapper.toBrand(request);

        // Auto-generate slug from name
        String slug = SlugUtils.toSlug(request.getName());

        // Ensure slug uniqueness
        int counter = 1;
        String originalSlug = slug;
        while (brandRepository.existsBySlug(slug)) {
            slug = SlugUtils.makeUnique(originalSlug, counter++);
        }
        brand.setSlug(slug);

        Brand savedBrand = brandRepository.save(brand);
        logger.info("Brand created: {} (slug: {})", savedBrand.getName(), savedBrand.getSlug());

        return brandMapper.toBrandResponse(savedBrand);
    }


    // GET ALL BRANDS

    @Override
    public List<BrandResponse> getAll() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toBrandResponse)
                .collect(Collectors.toList());
    }


    // GET BRAND BY ID

    @Override
    public BrandResponse getById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        return brandMapper.toBrandResponse(brand);
    }


    // UPDATE BRAND

    @Override
    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // Check for duplicate name (exclude current brand)
        if (!brand.getName().equals(request.getName()) && brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Brand", "name", request.getName());
        }

        // Update fields
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());

        // Re-generate slug if name changed
        if (!brand.getSlug().equals(SlugUtils.toSlug(request.getName()))) {
            String slug = SlugUtils.toSlug(request.getName());
            int counter = 1;
            String originalSlug = slug;
            while (brandRepository.existsBySlug(slug)) {
                slug = SlugUtils.makeUnique(originalSlug, counter++);
            }
            brand.setSlug(slug);
        }

        Brand updatedBrand = brandRepository.save(brand);
        logger.info("Brand updated: {} (id: {})", updatedBrand.getName(), updatedBrand.getId());

        return brandMapper.toBrandResponse(updatedBrand);
    }


    // DELETE BRAND

    @Override
    @Transactional
    public void delete(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        brandRepository.delete(brand);
        logger.info("Brand deleted: {} (id: {})", brand.getName(), id);
    }
}
