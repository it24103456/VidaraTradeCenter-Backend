package com.vidara.tradecenter.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NegativeStockException extends RuntimeException {

  private Long productId;
  private Integer attemptedStock;

  public NegativeStockException(String message) {
    super(message);
  }

  public NegativeStockException(Long productId, Integer attemptedStock) {
    super(String.format("Cannot set negative stock for product ID %d. Attempted value: %d",
        productId, attemptedStock));
    this.productId = productId;
    this.attemptedStock = attemptedStock;
  }

  public Long getProductId() {
    return productId;
  }

  public Integer getAttemptedStock() {
    return attemptedStock;
  }
}
