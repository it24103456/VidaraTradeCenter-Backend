package com.vidara.tradecenter.cart.service.impl;

import com.vidara.tradecenter.cart.dto.request.AddToCartRequest;
import com.vidara.tradecenter.cart.dto.request.UpdateCartItemRequest;
import com.vidara.tradecenter.cart.dto.response.CartResponse;
import com.vidara.tradecenter.cart.exception.CartNotFoundException;
import com.vidara.tradecenter.cart.exception.InsufficientStockException;
import com.vidara.tradecenter.cart.mapper.CartMapper;
import com.vidara.tradecenter.cart.model.Cart;
import com.vidara.tradecenter.cart.model.CartItem;
import com.vidara.tradecenter.cart.model.enums.CartStatus;
import com.vidara.tradecenter.cart.repository.CartItemRepository;
import com.vidara.tradecenter.cart.repository.CartRepository;
import com.vidara.tradecenter.cart.service.CartService;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.product.model.Product;
import com.vidara.tradecenter.product.repository.ProductRepository;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

  private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final CartMapper cartMapper;

  public CartServiceImpl(CartRepository cartRepository,
      CartItemRepository cartItemRepository,
      ProductRepository productRepository,
      UserRepository userRepository,
      CartMapper cartMapper) {
    this.cartRepository = cartRepository;
    this.cartItemRepository = cartItemRepository;
    this.productRepository = productRepository;
    this.userRepository = userRepository;
    this.cartMapper = cartMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public CartResponse getOrCreateCart(Long userId) {
    User user = getUserById(userId);
    Cart cart = getOrCreateActiveCart(user);
    return cartMapper.toCartResponse(cart);
  }

  @Override
  public CartResponse addToCart(Long userId, AddToCartRequest request) {
    logger.info("Adding product {} to cart for user {}: quantity {}",
        request.getProductId(), userId, request.getQuantity());

    User user = getUserById(userId);
    Product product = getProductById(request.getProductId());

    validateStockAvailability(product, request.getQuantity());

    Cart cart = getOrCreateActiveCart(user);
    Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

    if (existingItem.isPresent()) {
      logger.debug("Updating existing cart item for product {}", product.getSku());
      updateExistingCartItem(existingItem.get(), product, request.getQuantity());
    } else {
      logger.debug("Adding new cart item for product {}", product.getSku());
      addNewCartItem(cart, product, request.getQuantity());
    }

    CartResponse response = cartMapper.toCartResponse(cartRepository.save(cart));
    logger.info("Successfully added product {} to cart. Total items: {}",
        product.getSku(), response.getItems().size());
    return response;
  }

  @Override
  public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
    logger.info("Updating cart item {} for user {}: new quantity {}", cartItemId, userId, request.getQuantity());

    CartItem cartItem = getCartItemById(cartItemId);
    verifyCartOwnership(cartItem, userId);

    // Refresh product data to get latest stock information
    Product product = getProductById(cartItem.getProduct().getId());
    validateStockAvailability(product, request.getQuantity());

    Integer oldQuantity = cartItem.getQuantity();
    cartItem.setQuantity(request.getQuantity());
    cartItemRepository.save(cartItem);

    logger.info("Cart item {} updated: quantity {} -> {}", cartItemId, oldQuantity, request.getQuantity());
    return cartMapper.toCartResponse(cartItem.getCart());
  }

  @Override
  public CartResponse removeCartItem(Long userId, Long cartItemId) {
    logger.info("Removing cart item {} for user {}", cartItemId, userId);

    CartItem cartItem = getCartItemById(cartItemId);
    verifyCartOwnership(cartItem, userId);

    Cart cart = cartItem.getCart();
    String productSku = cartItem.getProduct().getSku();
    cart.removeItem(cartItem);
    cartItemRepository.delete(cartItem);

    logger.info("Cart item {} removed (product: {})", cartItemId, productSku);
    return cartMapper.toCartResponse(cart);
  }

  @Override
  public void clearCart(Long userId) {
    logger.info("Clearing cart for user {}", userId);

    User user = getUserById(userId);
    Cart cart = getActiveCart(user);

    int itemCount = cart.getItems().size();
    cartItemRepository.deleteByCartId(cart.getId());
    cart.getItems().clear();
    cartRepository.save(cart);

    logger.info("Cart cleared for user {}: {} items removed", userId, itemCount);
  }

  @Override
  @Transactional(readOnly = true)
  public CartResponse getActiveCart(Long userId) {
    User user = getUserById(userId);
    Cart cart = getActiveCart(user);
    return cartMapper.toCartResponse(cart);
  }

  @Override
  public CartResponse syncCartPrices(Long userId) {
    logger.info("Syncing cart prices for user {}", userId);

    User user = getUserById(userId);
    Cart cart = getActiveCart(user);

    int updatedCount = 0;
    int unchangedCount = 0;

    for (CartItem item : cart.getItems()) {
      Product product = item.getProduct();
      BigDecimal currentPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();

      if (!item.getPrice().equals(currentPrice)) {
        logger.info("Price changed for product {} in cart: {} -> {}",
            product.getSku(), item.getPrice(), currentPrice);
        item.setPrice(currentPrice);
        cartItemRepository.save(item);
        updatedCount++;
      } else {
        unchangedCount++;
      }
    }

    logger.info("Cart price sync completed for user {}: {} updated, {} unchanged",
        userId, updatedCount, unchangedCount);

    return cartMapper.toCartResponse(cartRepository.save(cart));
  }

  // PRIVATE HELPER METHODS

  private User getUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
  }

  private Product getProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
  }

  private CartItem getCartItemById(Long cartItemId) {
    return cartItemRepository.findById(cartItemId)
        .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));
  }

  private Cart getOrCreateActiveCart(User user) {
    return cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)
        .orElseGet(() -> cartRepository.save(new Cart(user)));
  }

  private Cart getActiveCart(User user) {
    return cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)
        .orElseThrow(() -> new CartNotFoundException(user.getId()));
  }

  private void validateStockAvailability(Product product, Integer requestedQuantity) {
    Integer availableStock = product.getStock() != null ? product.getStock() : 0;

    if (availableStock < requestedQuantity) {
      logger.warn("Insufficient stock for product {} ({}): available {}, requested {}",
          product.getSku(), product.getName(), availableStock, requestedQuantity);
      throw new InsufficientStockException(
          product.getName(),
          requestedQuantity,
          availableStock);
    }
  }

  private void verifyCartOwnership(CartItem cartItem, Long userId) {
    if (!cartItem.getCart().getUser().getId().equals(userId)) {
      throw new CartNotFoundException("Cart item does not belong to user");
    }
  }

  private void updateExistingCartItem(CartItem item, Product product, Integer additionalQuantity) {
    int newQuantity = item.getQuantity() + additionalQuantity;
    validateStockAvailability(product, newQuantity);
    item.setQuantity(newQuantity);
    cartItemRepository.save(item);
  }

  private void addNewCartItem(Cart cart, Product product, Integer quantity) {
    BigDecimal price = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();
    CartItem newItem = new CartItem(cart, product, quantity, price);
    cart.addItem(newItem);
    cartItemRepository.save(newItem);
  }
}
