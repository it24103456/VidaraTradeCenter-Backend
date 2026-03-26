package com.vidara.tradecenter.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StockOperationException extends RuntimeException {

  private String operation;
  private Long productId;

  public StockOperationException(String message) {
    super(message);
  }

  public StockOperationException(String operation, Long productId, String message) {
    super(String.format("Stock operation '%s' failed for product ID %d: %s",
        operation, productId, message));
    this.operation = operation;
    this.productId = productId;
  }

  public String getOperation() {
    return operation;
  }

  public Long getProductId() {
    return productId;
  }
}
