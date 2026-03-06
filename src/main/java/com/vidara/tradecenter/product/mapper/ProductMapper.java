package com.vidara.tradecenter.product.mapper;

import com.vidara.tradecenter.product.dto.request.ProductRequest;
import com.vidara.tradecenter.product.dto.response.BrandResponse;
import com.vidara.tradecenter.product.dto.response.CategoryResponse;
import com.vidara.tradecenter.product.dto.response.ProductDetailResponse;
import com.vidara.tradecenter.product.dto.response.ProductResponse;
import com.vidara.tradecenter.product.model.Product;
import com.vidara.tradecenter.product.model.ProductImage;
import com.vidara.tradecenter.product.model.ProductSpecification;
import com.vidara.tradecenter.product.model.Tag;
import com.vidara.tradecenter.product.model.enums.ProductStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    // Map ProductRequest DTO → Product entity (basic fields only)
    public Product toProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setBasePrice(request.getBasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());

        if (request.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(request.getStatus().toUpperCase()));
        }

        return product;
    }

    // Map Product entity → ProductResponse DTO (list/summary view)
    public ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setSku(product.getSku());
        response.setBasePrice(product.getBasePrice());
        response.setSalePrice(product.getSalePrice());
        response.setStatus(product.getStatus().name());

        // Primary image URL
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            product.getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .findFirst()
                    .ifPresentOrElse(
                            img -> response.setPrimaryImageUrl(img.getImageUrl()),
                            () -> response.setPrimaryImageUrl(product.getImages().get(0).getImageUrl())
                    );
        }

        // Category name
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }

        // Brand name
        if (product.getBrand() != null) {
            response.setBrandName(product.getBrand().getName());
        }

        return response;
    }

    // Map Product entity → ProductDetailResponse DTO (full detail view)
    public ProductDetailResponse toProductDetailResponse(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setSku(product.getSku());
        response.setBasePrice(product.getBasePrice());
        response.setSalePrice(product.getSalePrice());
        response.setStatus(product.getStatus().name());
        response.setDescription(product.getDescription());
        response.setWeight(product.getWeight());
        response.setDimensions(product.getDimensions());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Map category
        if (product.getCategory() != null) {
            CategoryResponse categoryResponse = new CategoryResponse();
            categoryResponse.setId(product.getCategory().getId());
            categoryResponse.setName(product.getCategory().getName());
            categoryResponse.setSlug(product.getCategory().getSlug());
            response.setCategory(categoryResponse);
        }

        // Map brand
        if (product.getBrand() != null) {
            BrandResponse brandResponse = new BrandResponse();
            brandResponse.setId(product.getBrand().getId());
            brandResponse.setName(product.getBrand().getName());
            brandResponse.setSlug(product.getBrand().getSlug());
            brandResponse.setLogoUrl(product.getBrand().getLogoUrl());
            response.setBrand(brandResponse);
        }

        // Map images
        if (product.getImages() != null) {
            List<ProductDetailResponse.ImageResponse> imageResponses = product.getImages().stream()
                    .map(this::toImageResponse)
                    .collect(Collectors.toList());
            response.setImages(imageResponses);
        }

        // Map specifications
        if (product.getSpecifications() != null) {
            List<ProductDetailResponse.SpecificationResponse> specResponses = product.getSpecifications().stream()
                    .map(this::toSpecificationResponse)
                    .collect(Collectors.toList());
            response.setSpecifications(specResponses);
        }

        // Map tags
        if (product.getTags() != null) {
            List<String> tagNames = product.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList());
            response.setTags(tagNames);
        }

        return response;
    }

    // Update existing Product entity from ProductRequest
    public void updateProductFromRequest(ProductRequest request, Product product) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setBasePrice(request.getBasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());

        if (request.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(request.getStatus().toUpperCase()));
        }
    }


    // PRIVATE HELPER METHODS

    private ProductDetailResponse.ImageResponse toImageResponse(ProductImage image) {
        ProductDetailResponse.ImageResponse response = new ProductDetailResponse.ImageResponse();
        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setAltText(image.getAltText());
        response.setSortOrder(image.getSortOrder());
        response.setPrimary(image.isPrimary());
        return response;
    }

    private ProductDetailResponse.SpecificationResponse toSpecificationResponse(ProductSpecification spec) {
        ProductDetailResponse.SpecificationResponse response = new ProductDetailResponse.SpecificationResponse();
        response.setId(spec.getId());
        response.setKey(spec.getSpecKey());
        response.setValue(spec.getSpecValue());
        return response;
    }
}
