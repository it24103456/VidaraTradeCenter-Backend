package com.vidara.tradecenter.inventory.dto.response;

import java.time.LocalDateTime;

public class StockAdjustmentResponse {

  private Long id;
  private Long productId;
  private String productName;
  private String productSku;
  private Integer quantityChange;
  private Integer stockBefore;
  private Integer stockAfter;
  private String adjustmentType;
  private String reason;
  private String adjustedBy;
  private LocalDateTime adjustedAt;

  // CONSTRUCTORS

  public StockAdjustmentResponse() {
  }

  // GETTERS AND SETTERS

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public String getProductSku() {
    return productSku;
  }

  public void setProductSku(String productSku) {
    this.productSku = productSku;
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

  public String getAdjustmentType() {
    return adjustmentType;
  }

  public void setAdjustmentType(String adjustmentType) {
    this.adjustmentType = adjustmentType;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getAdjustedBy() {
    return adjustedBy;
  }

  public void setAdjustedBy(String adjustedBy) {
    this.adjustedBy = adjustedBy;
  }

  public LocalDateTime getAdjustedAt() {
    return adjustedAt;
  }

  public void setAdjustedAt(LocalDateTime adjustedAt) {
    this.adjustedAt = adjustedAt;
  }
}
