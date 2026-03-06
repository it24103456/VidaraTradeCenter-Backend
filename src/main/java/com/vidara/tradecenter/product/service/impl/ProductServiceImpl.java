package com.vidara.tradecenter.product.service.impl;

import com.vidara.tradecenter.common.dto.PagedResponse;
import com.vidara.tradecenter.common.exception.DuplicateResourceException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.common.util.SlugUtils;
import com.vidara.tradecenter.product.dto.request.ProductRequest;
import com.vidara.tradecenter.product.dto.response.ProductDetailResponse;
import com.vidara.tradecenter.product.dto.response.ProductResponse;
import com.vidara.tradecenter.product.mapper.ProductMapper;
import com.vidara.tradecenter.product.model.*;
import com.vidara.tradecenter.product.model.enums.ProductStatus;
import com.vidara.tradecenter.product.repository.*;
import com.vidara.tradecenter.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final TagRepository tagRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              BrandRepository brandRepository,
                              TagRepository tagRepository,
                              ProductImageRepository productImageRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.tagRepository = tagRepository;
        this.productImageRepository = productImageRepository;
        this.productMapper = productMapper;
    }


    // CREATE PRODUCT

    @Override
    @Transactional
    public ProductDetailResponse create(ProductRequest request) {
        // Validate SKU uniqueness
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        Product product = productMapper.toProduct(request);

        // Auto-generate slug from name
        String slug = SlugUtils.toSlug(request.getName());
        int counter = 1;
        String originalSlug = slug;
        while (productRepository.findBySlug(slug).isPresent()) {
            slug = SlugUtils.makeUnique(originalSlug, counter++);
        }
        product.setSlug(slug);

        // Set category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        // Set brand
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
            product.setBrand(brand);
        }

        // Save product first to get the ID
        Product savedProduct = productRepository.save(product);

        // Handle tags (create if not exist)
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName.trim())
                        .orElseGet(() -> {
                            Tag newTag = new Tag(tagName.trim(), SlugUtils.toSlug(tagName.trim()));
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            savedProduct.setTags(tags);
        }

        // Handle specifications
        if (request.getSpecifications() != null && !request.getSpecifications().isEmpty()) {
            for (ProductRequest.SpecificationEntry entry : request.getSpecifications()) {
                ProductSpecification spec = new ProductSpecification(entry.getKey(), entry.getValue());
                savedProduct.addSpecification(spec);
            }
        }

        // Handle images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = new ProductImage();
                image.setImageUrl(request.getImageUrls().get(i));
                image.setSortOrder(i);
                image.setPrimary(i == 0);
                savedProduct.addImage(image);
            }
        }

        savedProduct = productRepository.save(savedProduct);
        logger.info("Product created: {} (sku: {})", savedProduct.getName(), savedProduct.getSku());

        return productMapper.toProductDetailResponse(savedProduct);
    }


    // GET ALL PRODUCTS

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductResponse> content = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(content, productPage);
    }


    // GET PRODUCT BY ID

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return productMapper.toProductDetailResponse(product);
    }


    // GET PRODUCT BY SLUG

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));

        return productMapper.toProductDetailResponse(product);
    }


    // UPDATE PRODUCT

    @Override
    @Transactional
    public ProductDetailResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check SKU uniqueness (exclude current product)
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        // Update basic fields
        productMapper.updateProductFromRequest(request, product);

        // Re-generate slug if name changed
        if (!product.getSlug().equals(SlugUtils.toSlug(request.getName()))) {
            String slug = SlugUtils.toSlug(request.getName());
            int counter = 1;
            String originalSlug = slug;
            while (productRepository.findBySlug(slug).isPresent()) {
                slug = SlugUtils.makeUnique(originalSlug, counter++);
            }
            product.setSlug(slug);
        }

        // Update category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        // Update brand
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        // Update tags
        if (request.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName.trim())
                        .orElseGet(() -> {
                            Tag newTag = new Tag(tagName.trim(), SlugUtils.toSlug(tagName.trim()));
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            product.setTags(tags);
        }

        // Update specifications (replace all)
        if (request.getSpecifications() != null) {
            product.getSpecifications().clear();
            for (ProductRequest.SpecificationEntry entry : request.getSpecifications()) {
                ProductSpecification spec = new ProductSpecification(entry.getKey(), entry.getValue());
                product.addSpecification(spec);
            }
        }

        // Update images (replace all)
        if (request.getImageUrls() != null) {
            product.getImages().clear();
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = new ProductImage();
                image.setImageUrl(request.getImageUrls().get(i));
                image.setSortOrder(i);
                image.setPrimary(i == 0);
                product.addImage(image);
            }
        }

        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated: {} (id: {})", updatedProduct.getName(), updatedProduct.getId());

        return productMapper.toProductDetailResponse(updatedProduct);
    }


    // DELETE PRODUCT

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(product);
        logger.info("Product deleted: {} (id: {})", product.getName(), id);
    }


    // FILTER PRODUCTS

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> filter(Long categoryId, Long brandId,
                                                  Double minPrice, Double maxPrice,
                                                  String search,
                                                  int page, int size,
                                                  String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Build dynamic query using JPA Specification
        Specification<Product> spec = Specification.where(null);

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        if (brandId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("basePrice"), BigDecimal.valueOf(minPrice)));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("basePrice"), BigDecimal.valueOf(maxPrice)));
        }

        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.trim().toLowerCase();
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + keyword + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword + "%")
                    ));
        }

        // Only show ACTIVE products in public filter
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), ProductStatus.ACTIVE));

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductResponse> content = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(content, productPage);
    }
}
