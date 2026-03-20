package com.vidara.tradecenter.inventory.model;

import com.vidara.tradecenter.common.base.BaseEntity;
import com.vidara.tradecenter.inventory.model.enums.StockAdjustmentType;
import com.vidara.tradecenter.product.model.Product;
import com.vidara.tradecenter.user.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "stock_adjustments", indexes = {
    @Index(name = "idx_stock_adj_product", columnList = "product_id"),
    @Index(name = "idx_stock_adj_type", columnList = "adjustment_type"),
    @Index(name = "idx_stock_adj_date", columnList = "created_at")
})
public class StockAdjustment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "quantity_change", nullable = false)
  private Integer quantityChange;

  @Column(name = "stock_before", nullable = false)
  private Integer stockBefore;

  @Column(name = "stock_after", nullable = false)
  private Integer stockAfter;

  @Enumerated(EnumType.STRING)
  @Column(name = "adjustment_type", nullable = false, length = 30)
  private StockAdjustmentType adjustmentType;

  @Column(name = "reason", length = 500)
  private String reason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "adjusted_by")
  private User adjustedBy;

  // CONSTRUCTORS

  public StockAdjustment() {
  }

  public StockAdjustment(Product product, Integer quantityChange, Integer stockBefore,
      Integer stockAfter, StockAdjustmentType adjustmentType, String reason) {
    this.product = product;
    this.quantityChange = quantityChange;
    this.stockBefore = stockBefore;
    this.stockAfter = stockAfter;
    this.adjustmentType = adjustmentType;
    this.reason = reason;
  }

  // GETTERS AND SETTERS

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public Integer getQuantityChange() {
    return quantityChange;
  }

  public void setQuantityChange(Integer quantityChange) {
    this.quantityChange = quantityChange;
  }

  public Integer getStockBefore() {
    return stockBefore;
  }

  public void setStockBefore(Integer stockBefore) {
    this.stockBefore = stockBefore;
  }

  public Integer getStockAfter() {
    return stockAfter;
  }

  public void setStockAfter(Integer stockAfter) {
    this.stockAfter = stockAfter;
  }

  public StockAdjustmentType getAdjustmentType() {
    return adjustmentType;
  }

  public void setAdjustmentType(StockAdjustmentType adjustmentType) {
    this.adjustmentType = adjustmentType;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public User getAdjustedBy() {
    return adjustedBy;
  }

  public void setAdjustedBy(User adjustedBy) {
    this.adjustedBy = adjustedBy;
  }
}
