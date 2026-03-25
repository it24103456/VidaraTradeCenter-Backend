package com.vidara.tradecenter.cart.model;

import com.vidara.tradecenter.cart.model.enums.CartStatus;
import com.vidara.tradecenter.common.base.BaseEntity;
import com.vidara.tradecenter.user.model.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Shopping cart entity representing a user's cart.
 * Contains cart items and tracks cart status (ACTIVE, MERGED_TO_ORDER,
 * ABANDONED).
 * Each user can have one active cart at a time.
 */
@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id"),
    @Index(name = "idx_cart_status", columnList = "status")
})
public class Cart extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private CartStatus status = CartStatus.ACTIVE;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CartItem> items = new ArrayList<>();

  // CONSTRUCTORS

  public Cart() {
  }

  public Cart(User user) {
    this.user = user;
    this.status = CartStatus.ACTIVE;
  }

  // HELPER METHODS

  /**
   * Adds an item to the cart and establishes bidirectional relationship.
   *
   * @param item the cart item to add
   */
  public void addItem(CartItem item) {
    items.add(item);
    item.setCart(this);
  }

  /**
   * Removes an item from the cart and breaks bidirectional relationship.
   *
   * @param item the cart item to remove
   */
  public void removeItem(CartItem item) {
    items.remove(item);
    item.setCart(null);
  }

  /**
   * Calculates the total amount of all items in the cart.
   *
   * @return total cart amount (sum of all item subtotals)
   */
  public BigDecimal getTotalAmount() {
    return items.stream()
        .map(CartItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculates the total number of items in the cart.
   *
   * @return total quantity of all items
   */
  public int getTotalItems() {
    return items.stream()
        .mapToInt(CartItem::getQuantity)
        .sum();
  }

  // GETTERS AND SETTERS

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public CartStatus getStatus() {
    return status;
  }

  public void setStatus(CartStatus status) {
    this.status = status;
  }

  public List<CartItem> getItems() {
    return items;
  }

  public void setItems(List<CartItem> items) {
    this.items = items;
  }
}
