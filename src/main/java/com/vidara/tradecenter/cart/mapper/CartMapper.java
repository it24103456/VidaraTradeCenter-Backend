package com.vidara.tradecenter.cart.mapper;

import com.vidara.tradecenter.cart.dto.response.CartItemResponse;
import com.vidara.tradecenter.cart.dto.response.CartResponse;
import com.vidara.tradecenter.cart.model.Cart;
import com.vidara.tradecenter.cart.model.CartItem;
import com.vidara.tradecenter.product.model.Product;
import com.vidara.tradecenter.product.model.ProductImage;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting Cart entities to DTOs.
 * Handles transformation of cart and cart item data for API responses.
 */
@Component
public class CartMapper {

  /**
   * Converts Cart entity to CartResponse DTO.
   * Includes all cart items, totals, and status information.
   *
   * @param cart the cart entity to convert
   * @return CartResponse DTO with complete cart information
   */
  public CartResponse toCartResponse(Cart cart) {
    CartResponse response = new CartResponse();
    response.setId(cart.getId());
    response.setUserId(cart.getUser().getId());
    response.setStatus(cart.getStatus().name());
    response.setItems(cart.getItems().stream()
        .map(this::toCartItemResponse)
        .collect(Collectors.toList()));
    response.setTotalAmount(cart.getTotalAmount());
    response.setTotalItems(cart.getTotalItems());
    return response;
  }

  /**
   * Converts CartItem entity to CartItemResponse DTO.
   * Includes product details, pricing, and quantity information.
   *
   * @param cartItem the cart item entity to convert
   * @return CartItemResponse DTO with item details
   */
  public CartItemResponse toCartItemResponse(CartItem cartItem) {
    CartItemResponse response = new CartItemResponse();
    response.setId(cartItem.getId());

    // Map product information
    Product product = cartItem.getProduct();
    response.setProductId(product.getId());
    response.setProductName(product.getName());
    response.setProductSlug(product.getSlug());

    // Set product image (primary image or first available)
    setProductImage(response, product);

    // Map pricing and quantity information
    response.setPrice(cartItem.getPrice());
    response.setQuantity(cartItem.getQuantity());
    response.setSubtotal(cartItem.getSubtotal());
    response.setPriceAtAddition(cartItem.getPriceAtAddition());
    response.setPriceChanged(cartItem.hasPriceChanged());

    return response;
  }

  /**
   * Sets the product image URL in the response.
   * Prioritizes primary image, falls back to first available image.
   *
   * @param response the response object to update
   * @param product  the product containing images
   */
  private void setProductImage(CartItemResponse response, Product product) {
    if (product.getImages() != null && !product.getImages().isEmpty()) {
      ProductImage primaryImage = product.getImages().stream()
          .filter(ProductImage::isPrimary)
          .findFirst()
          .orElse(product.getImages().get(0));
      response.setProductImage(primaryImage.getImageUrl());
    }
  }
}
