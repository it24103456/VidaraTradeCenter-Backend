package com.vidara.tradecenter.cart.model;

import com.vidara.tradecenter.common.base.BaseEntity;
import com.vidara.tradecenter.product.model.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Cart item entity representing a product in a shopping cart.
 * Stores product reference, quantity, and price snapshot at time of addition.
 * Price snapshot allows tracking price changes between cart addition and
 * checkout.
 */
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
    @Index(name = "idx_cart_item_product", columnList = "product_id")
})
public class CartItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "price_at_addition", precision = 10, scale = 2)
  private BigDecimal priceAtAddition;

  // CONSTRUCTORS

  public CartItem() {
  }

  public CartItem(Cart cart, Product product, Integer quantity, BigDecimal price) {
    this.cart = cart;
    this.product = product;
    this.quantity = quantity;
    this.price = price;
    this.priceAtAddition = price;
  }

  // HELPER METHODS

  /**
   * Calculates the subtotal for this cart item (price × quantity).
   *
   * @return subtotal amount for this item
   */
  public BigDecimal getSubtotal() {
    return price.multiply(BigDecimal.valueOf(quantity));
  }

  /**
   * Checks if the current price differs from the price at addition.
   * Useful for alerting users about price changes.
   *
   * @return true if price has changed since addition
   */
  public boolean hasPriceChanged() {
    return priceAtAddition != null && !price.equals(priceAtAddition);
  }

  /**
   * Calculates the price difference between current and original price.
   *
   * @return price difference (positive if increased, negative if decreased)
   */
  public BigDecimal getPriceDifference() {
    if (priceAtAddition == null) {
      return BigDecimal.ZERO;
    }
    return price.subtract(priceAtAddition);
  }

  // GETTERS AND SETTERS

  public Cart getCart() {
    return cart;
  }

  public void setCart(Cart cart) {
    this.cart = cart;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public BigDecimal getPriceAtAddition() {
    return priceAtAddition;
  }

  public void setPriceAtAddition(BigDecimal priceAtAddition) {
    this.priceAtAddition = priceAtAddition;
  }
}
