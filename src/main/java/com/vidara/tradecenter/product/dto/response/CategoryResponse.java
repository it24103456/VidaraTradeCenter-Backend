package com.vidara.tradecenter.product.dto.response;

import java.util.List;

public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private Long parentId;
    private List<CategoryResponse> subcategories;
    private long productCount;


    // CONSTRUCTORS

    public CategoryResponse() {
    }


    // GETTERS AND SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<CategoryResponse> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<CategoryResponse> subcategories) {
        this.subcategories = subcategories;
    }

    public long getProductCount() {
        return productCount;
    }

    public void setProductCount(long productCount) {
        this.productCount = productCount;
    }
}
