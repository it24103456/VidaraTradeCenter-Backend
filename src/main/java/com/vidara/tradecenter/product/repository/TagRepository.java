package com.vidara.tradecenter.product.repository;

import com.vidara.tradecenter.product.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // Find tag by name
    Optional<Tag> findByName(String name);
}
