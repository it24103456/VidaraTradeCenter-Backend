package com.vidara.tradecenter.cart.service;

import com.vidara.tradecenter.cart.dto.request.AddToCartRequest;
import com.vidara.tradecenter.cart.dto.request.UpdateCartItemRequest;
import com.vidara.tradecenter.cart.dto.response.CartResponse;

public interface CartService {

  /**
   * Get or create active cart for the current user
   */
  CartResponse getOrCreateCart(Long userId);

  /**
   * Add item to cart or update quantity if already exists
   */
  CartResponse addToCart(Long userId, AddToCartRequest request);

  /**
   * Update cart item quantity
   */
  CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request);

  /**
   * Remove item from cart
   */
  CartResponse removeCartItem(Long userId, Long cartItemId);

  /**
   * Clear all items from cart
   */
  void clearCart(Long userId);

  /**
   * Get active cart for user
   */
  CartResponse getActiveCart(Long userId);

  /**
   * Sync cart item prices with current product prices
   */
  CartResponse syncCartPrices(Long userId);
}
