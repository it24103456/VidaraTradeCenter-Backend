package com.vidara.tradecenter.inventory.repository;

import com.vidara.tradecenter.inventory.model.StockAdjustment;
import com.vidara.tradecenter.inventory.model.enums.StockAdjustmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {

  Page<StockAdjustment> findByProductId(Long productId, Pageable pageable);

  List<StockAdjustment> findByProductIdOrderByCreatedAtDesc(Long productId);

  Page<StockAdjustment> findByAdjustmentType(StockAdjustmentType adjustmentType, Pageable pageable);
}
