package com.vidara.tradecenter.product.mapper;

import com.vidara.tradecenter.product.dto.request.BrandRequest;
import com.vidara.tradecenter.product.dto.response.BrandResponse;
import com.vidara.tradecenter.product.model.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {

    // Map BrandRequest DTO → Brand entity
    public Brand toBrand(BrandRequest request) {
        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        return brand;
    }

    // Map Brand entity → BrandResponse DTO
    public BrandResponse toBrandResponse(Brand brand) {
        BrandResponse response = new BrandResponse();
        response.setId(brand.getId());
        response.setName(brand.getName());
        response.setDescription(brand.getDescription());
        response.setSlug(brand.getSlug());
        response.setLogoUrl(brand.getLogoUrl());
        return response;
    }
}
