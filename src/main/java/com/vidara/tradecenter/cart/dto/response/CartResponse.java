package com.vidara.tradecenter.cart.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartResponse {

  private Long id;
  private Long userId;
  private String status;
  private List<CartItemResponse> items = new ArrayList<>();
  private BigDecimal totalAmount;
  private Integer totalItems;

  // CONSTRUCTORS

  public CartResponse() {
  }

  // GETTERS AND SETTERS

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<CartItemResponse> getItems() {
    return items;
  }

  public void setItems(List<CartItemResponse> items) {
    this.items = items;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public Integer getTotalItems() {
    return totalItems;
  }

  public void setTotalItems(Integer totalItems) {
    this.totalItems = totalItems;
  }
}
