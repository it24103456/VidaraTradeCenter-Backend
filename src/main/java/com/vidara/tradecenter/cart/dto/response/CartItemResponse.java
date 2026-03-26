package com.vidara.tradecenter.cart.dto.response;

import java.math.BigDecimal;

public class CartItemResponse {

  private Long id;
  private Long productId;
  private String productName;
  private String productSlug;
  private String productImage;
  private BigDecimal price;
  private Integer quantity;
  private BigDecimal subtotal;
  private BigDecimal priceAtAddition;
  private Boolean priceChanged;

  // CONSTRUCTORS

  public CartItemResponse() {
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

  public String getProductSlug() {
    return productSlug;
  }

  public void setProductSlug(String productSlug) {
    this.productSlug = productSlug;
  }

  public String getProductImage() {
    return productImage;
  }

  public void setProductImage(String productImage) {
    this.productImage = productImage;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }

  public BigDecimal getPriceAtAddition() {
    return priceAtAddition;
  }

  public void setPriceAtAddition(BigDecimal priceAtAddition) {
    this.priceAtAddition = priceAtAddition;
  }

  public Boolean getPriceChanged() {
    return priceChanged;
  }

  public void setPriceChanged(Boolean priceChanged) {
    this.priceChanged = priceChanged;
  }
}
