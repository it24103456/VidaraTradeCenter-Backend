package com.vidara.tradecenter.product.model;

import com.vidara.tradecenter.common.base.BaseEntity;
import com.vidara.tradecenter.product.model.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug", columnList = "slug"),
        @Index(name = "idx_product_sku", columnList = "sku"),
        @Index(name = "idx_product_status", columnList = "status")
})
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "slug", nullable = false, unique = true, length = 250)
    private String slug;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "weight", precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "stock")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock = 0;

    @Column(name = "low_stock_threshold")
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold = 10;

    // RELATIONSHIPS

    // Many-to-One with Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Many-to-One with Brand
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    // One-to-Many with ProductImage
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    // One-to-Many with ProductSpecification
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSpecification> specifications = new ArrayList<>();

    // Many-to-Many with Tag (owning side)
    @ManyToMany
    @JoinTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    // CONSTRUCTORS

    public Product() {
    }

    public Product(String name, String slug, String sku, BigDecimal basePrice) {
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.basePrice = basePrice;
        this.status = ProductStatus.DRAFT;
    }

    // HELPER METHODS

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    public void addSpecification(ProductSpecification specification) {
        specifications.add(specification);
        specification.setProduct(this);
    }

    public void removeSpecification(ProductSpecification specification) {
        specifications.remove(specification);
        specification.setProduct(null);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    // GETTERS AND SETTERS

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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public Integer getStock() {
        return stock;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public void setStock(Integer stock) {
        if (stock != null && stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stock = stock;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public boolean isLowStock() {
        return stock != null && lowStockThreshold != null && stock <= lowStockThreshold;
    }

    public boolean isOutOfStock() {
        return stock == null || stock == 0;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public List<ProductSpecification> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<ProductSpecification> specifications) {
        this.specifications = specifications;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
