package com.vidara.tradecenter.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StockAdjustmentRequest {

  @NotNull(message = "Product ID is required")
  private Long productId;

  @NotNull(message = "Quantity change is required")
  private Integer quantityChange;

  @Size(max = 500, message = "Reason must be less than 500 characters")
  private String reason;

  // CONSTRUCTORS

  public StockAdjustmentRequest() {
  }

  public StockAdjustmentRequest(Long productId, Integer quantityChange, String reason) {
    this.productId = productId;
    this.quantityChange = quantityChange;
    this.reason = reason;
  }

  // GETTERS AND SETTERS

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Integer getQuantityChange() {
    return quantityChange;
  }

  public void setQuantityChange(Integer quantityChange) {
    this.quantityChange = quantityChange;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
