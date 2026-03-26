package com.vidara.tradecenter.cart.controller;

import com.vidara.tradecenter.cart.dto.request.AddToCartRequest;
import com.vidara.tradecenter.cart.dto.request.UpdateCartItemRequest;
import com.vidara.tradecenter.cart.dto.response.CartResponse;
import com.vidara.tradecenter.cart.service.CartService;
import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for shopping cart operations.
 * Handles cart management including adding, updating, and removing items.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart management APIs")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get active cart", description = "Get the current user's active shopping cart")
  public ResponseEntity<ApiResponse<CartResponse>> getCart(@CurrentUser CustomUserDetails currentUser) {
    CartResponse cart = cartService.getOrCreateCart(currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
  }

  @PostMapping("/add")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Add item to cart", description = "Add a product to the shopping cart or update quantity if already exists")
  public ResponseEntity<ApiResponse<CartResponse>> addToCart(
      @CurrentUser CustomUserDetails currentUser,
      @Valid @RequestBody AddToCartRequest request) {
    CartResponse cart = cartService.addToCart(currentUser.getId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Item added to cart successfully", cart));
  }

  @PutMapping("/items/{cartItemId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Update cart item", description = "Update the quantity of a cart item")
  public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
      @CurrentUser CustomUserDetails currentUser,
      @PathVariable Long cartItemId,
      @Valid @RequestBody UpdateCartItemRequest request) {
    CartResponse cart = cartService.updateCartItem(currentUser.getId(), cartItemId, request);
    return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
  }

  @DeleteMapping("/items/{cartItemId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Remove cart item", description = "Remove an item from the shopping cart")
  public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
      @CurrentUser CustomUserDetails currentUser,
      @PathVariable Long cartItemId) {
    CartResponse cart = cartService.removeCartItem(currentUser.getId(), cartItemId);
    return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
  }

  @DeleteMapping("/clear")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Clear cart", description = "Remove all items from the shopping cart")
  public ResponseEntity<ApiResponse<Void>> clearCart(@CurrentUser CustomUserDetails currentUser) {
    cartService.clearCart(currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
  }

  @PostMapping("/sync-prices")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Sync cart prices", description = "Update cart item prices to match current product prices")
  public ResponseEntity<ApiResponse<CartResponse>> syncCartPrices(@CurrentUser CustomUserDetails currentUser) {
    CartResponse cart = cartService.syncCartPrices(currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Cart prices synchronized successfully", cart));
  }
}
