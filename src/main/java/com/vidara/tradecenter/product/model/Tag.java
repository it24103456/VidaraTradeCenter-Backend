package com.vidara.tradecenter.product.model;

import com.vidara.tradecenter.common.base.BaseEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tag_slug", columnList = "slug")
})
public class Tag extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;


    // RELATIONSHIPS

    // Many-to-Many with Product (inverse side)
    @ManyToMany(mappedBy = "tags")
    private Set<Product> products = new HashSet<>();


    // CONSTRUCTORS

    public Tag() {
    }

    public Tag(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }
}
