package com.vidara.tradecenter.inventory.service.impl;

import com.vidara.tradecenter.cart.exception.InsufficientStockException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.inventory.dto.request.StockAdjustmentRequest;
import com.vidara.tradecenter.inventory.dto.response.LowStockProductResponse;
import com.vidara.tradecenter.inventory.dto.response.StockAdjustmentResponse;
import com.vidara.tradecenter.inventory.model.StockAdjustment;
import com.vidara.tradecenter.inventory.model.enums.StockAdjustmentType;
import com.vidara.tradecenter.inventory.repository.StockAdjustmentRepository;
import com.vidara.tradecenter.inventory.service.InventoryService;
import com.vidara.tradecenter.product.model.Product;
import com.vidara.tradecenter.product.repository.ProductRepository;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

  private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

  private final ProductRepository productRepository;
  private final StockAdjustmentRepository stockAdjustmentRepository;
  private final UserRepository userRepository;

  public InventoryServiceImpl(ProductRepository productRepository,
      StockAdjustmentRepository stockAdjustmentRepository,
      UserRepository userRepository) {
    this.productRepository = productRepository;
    this.stockAdjustmentRepository = stockAdjustmentRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean checkStockAvailability(Long productId, Integer quantity) {
    // Checks whether the requested quantity is available for the given product.
    logger.debug("Checking stock availability for product ID {}: requested quantity {}", productId, quantity);

    Product product = getProductById(productId);
    boolean available = product.getStock() != null && product.getStock() >= quantity;

    if (!available) {
      logger.warn("Insufficient stock for product {} ({}): available {}, requested {}",
          product.getSku(), product.getName(), product.getStock(), quantity);
    }

    return available;
  }

  @Override
  public void reduceStock(Long productId, Integer quantity, String reason) {
    // Reduces stock when an order is placed.
    // Ensures enough stock is available before deduction
    // and records the stock movement for tracking.
    logger.info("Reducing stock for product ID {}: quantity {}, reason: {}", productId, quantity, reason);

    Product product = getProductById(productId);
    Integer currentStock = getCurrentStock(product);

    validateSufficientStock(product, currentStock, quantity);

    updateProductStock(product, currentStock - quantity);
    recordAdjustment(product, -quantity, currentStock, currentStock - quantity,
        StockAdjustmentType.ORDER_PLACED, reason, null);

    logger.info("Stock reduced for product {}: {} -> {} (quantity: {})",
        product.getSku(), currentStock, currentStock - quantity, quantity);
  }

  @Override
  public void restoreStock(Long productId, Integer quantity, String reason) {
    // Restores stock when an order is cancelled or reversed.
    // Increases stock and records the adjustment for audit history.
    logger.info("Restoring stock for product ID {}: quantity {}, reason: {}", productId, quantity, reason);

    Product product = getProductById(productId);
    Integer currentStock = getCurrentStock(product);
    Integer newStock = currentStock + quantity;

    updateProductStock(product, newStock);
    recordAdjustment(product, quantity, currentStock, newStock,
        StockAdjustmentType.ORDER_CANCELLED, reason, null);

    logger.info("Stock restored for product {}: {} -> {} (quantity: {})",
        product.getSku(), currentStock, newStock, quantity);
  }

  @Override
  public StockAdjustmentResponse adjustStock(StockAdjustmentRequest request, Long userId) {
    // Handles manual stock adjustments by admin users.
    // Calculates the updated stock using the adjustment amount,
    // validates that stock does not become negative,
    // then saves both the stock update and adjustment history.
    Product product = getProductById(request.getProductId());
    User user = getUserById(userId);

    Integer currentStock = getCurrentStock(product);
    Integer newStock = currentStock + request.getQuantityChange();

    validateNonNegativeStock(newStock);

    updateProductStock(product, newStock);
    StockAdjustment adjustment = recordAdjustment(product, request.getQuantityChange(),
        currentStock, newStock,
        StockAdjustmentType.MANUAL_ADJUSTMENT,
        request.getReason(), user);

    logger.info("Manual stock adjustment for product {} by user {}: {} -> {} (change: {})",
        product.getSku(), user.getEmail(), currentStock, newStock, request.getQuantityChange());

    return toStockAdjustmentResponse(adjustment);
  }

  @Override
  public void recordStockAdjustment(Long productId, Integer quantityChange,
      StockAdjustmentType type, String reason, Long userId) {
    // Records a stock change directly using product ID, quantity change,
    // adjustment type, reason, and optional user information.
    Product product = getProductById(productId);
    Integer currentStock = getCurrentStock(product);
    Integer newStock = currentStock + quantityChange;

    updateProductStock(product, newStock);

    User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
    recordAdjustment(product, quantityChange, currentStock, newStock, type, reason, user);
  }

  @Override
  @Transactional(readOnly = true)
  public List<LowStockProductResponse> getLowStockProducts() {
    // Retrieves products with stock at or below the configured threshold.
    // Used by admins to identify items that need restocking soon.
    logger.debug("Fetching low stock products");

    List<LowStockProductResponse> products = productRepository.findLowStockProducts(Pageable.unpaged()).stream()
        .map(this::toLowStockProductResponse)
        .collect(Collectors.toList());

    logger.info("Found {} low stock products", products.size());
    return products;
  }

  @Override
  @Transactional(readOnly = true)
  public List<LowStockProductResponse> getOutOfStockProducts() {
    // Retrieves products that are completely out of stock.
    logger.debug("Fetching out of stock products");

    List<LowStockProductResponse> products = productRepository.findOutOfStockProducts(Pageable.unpaged()).stream()
        .map(this::toLowStockProductResponse)
        .collect(Collectors.toList());

    logger.info("Found {} out of stock products", products.size());
    return products;
  }

  @Override
  @Transactional(readOnly = true)
  public List<StockAdjustmentResponse> getStockHistory(Long productId) {
    // Returns stock adjustment history for a product,
    // ordered from most recent to oldest.
    logger.debug("Fetching stock adjustment history for product ID {}", productId);

    List<StockAdjustmentResponse> history = stockAdjustmentRepository.findByProductIdOrderByCreatedAtDesc(productId)
        .stream()
        .map(this::toStockAdjustmentResponse)
        .collect(Collectors.toList());

    logger.info("Retrieved {} stock adjustment records for product ID {}", history.size(), productId);
    return history;
  }

  // =========================
  // PRIVATE HELPER METHODS
  // =========================

  private Product getProductById(Long productId) {
    // Finds a product by ID or throws an exception if not found.
    return productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
  }

  private User getUserById(Long userId) {
    // Finds a user by ID or throws an exception if not found.
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
  }

  private Integer getCurrentStock(Product product) {
    // Returns the current stock value.
    // Defaults to 0 if the product stock is null.
    return product.getStock() != null ? product.getStock() : 0;
  }

  private void updateProductStock(Product product, Integer newStock) {
    // Updates the product stock in the database with the new value.
    product.setStock(newStock);
    productRepository.save(product);
  }

  private void validateSufficientStock(Product product, Integer currentStock, Integer requestedQuantity) {
    // Validates that enough stock exists before reducing it.
    // Throws an exception if the requested quantity exceeds available stock.
    if (currentStock < requestedQuantity) {
      throw new InsufficientStockException(product.getName(), requestedQuantity, currentStock);
    }
  }

  private void validateNonNegativeStock(Integer stock) {
    // Ensures that stock does not become negative after adjustment.
    // Throws an exception if the resulting stock is invalid.
    if (stock < 0) {
      throw new IllegalArgumentException("Stock update would result in negative inventory");
    }
  }

  private StockAdjustment recordAdjustment(Product product, Integer quantityChange,
      Integer stockBefore, Integer stockAfter,
      StockAdjustmentType type, String reason, User user) {
    // Creates and saves a stock adjustment record.
    // Stores before/after stock values, adjustment type,
    // reason, and the user who performed the action.
    StockAdjustment adjustment = new StockAdjustment(product, quantityChange,
        stockBefore, stockAfter, type, reason);

    if (user != null) {
      adjustment.setAdjustedBy(user);
    }

    return stockAdjustmentRepository.save(adjustment);
  }

  private StockAdjustmentResponse toStockAdjustmentResponse(StockAdjustment adjustment) {
    // Converts a StockAdjustment entity into a response DTO
    // for API responses and history views.
    StockAdjustmentResponse response = new StockAdjustmentResponse();
    response.setId(adjustment.getId());
    response.setProductId(adjustment.getProduct().getId());
    response.setProductName(adjustment.getProduct().getName());
    response.setProductSku(adjustment.getProduct().getSku());
    response.setQuantityChange(adjustment.getQuantityChange());
    response.setStockBefore(adjustment.getStockBefore());
    response.setStockAfter(adjustment.getStockAfter());
    response.setAdjustmentType(adjustment.getAdjustmentType().name());
    response.setReason(adjustment.getReason());

    if (adjustment.getAdjustedBy() != null) {
      response.setAdjustedBy(adjustment.getAdjustedBy().getFullName());
    }

    response.setAdjustedAt(adjustment.getCreatedAt());
    return response;
  }

  private LowStockProductResponse toLowStockProductResponse(Product product) {
    // Converts a Product entity into a low-stock response DTO
    // for admin inventory monitoring.
    return new LowStockProductResponse(
        product.getId(),
        product.getName(),
        product.getSku(),
        product.getStock(),
        product.getLowStockThreshold(),
        product.getStatus().name());
  }

  @Override
  public void bulkReduceStock(List<Long> productIds, List<Integer> quantities, String reason) {
    // Reduces stock for multiple products in one operation.
    // Tracks how many updates succeeded or failed for logging.
    if (productIds.size() != quantities.size()) {
      throw new IllegalArgumentException("Product IDs and quantities lists must have the same size");
    }

    logger.info("Starting bulk stock reduction for {} products. Reason: {}", productIds.size(), reason);
    int successCount = 0;
    int failureCount = 0;

    for (int i = 0; i < productIds.size(); i++) {
      Long productId = productIds.get(i);
      Integer quantity = quantities.get(i);

      try {
        reduceStock(productId, quantity, reason);
        successCount++;
        logger.debug("Successfully reduced stock for product ID {}: quantity {}", productId, quantity);
      } catch (Exception e) {
        failureCount++;
        logger.error("Failed to reduce stock for product ID {}: {}", productId, e.getMessage());
      }
    }

    logger.info("Bulk stock reduction completed. Success: {}, Failures: {}", successCount, failureCount);
  }

  @Override
  public void bulkRestoreStock(List<Long> productIds, List<Integer> quantities, String reason) {
    // Restores stock for multiple products in bulk.
    // Useful for batch cancellations or inventory corrections.
    if (productIds.size() != quantities.size()) {
      throw new IllegalArgumentException("Product IDs and quantities lists must have the same size");
    }

    logger.info("Starting bulk stock restoration for {} products. Reason: {}", productIds.size(), reason);
    int successCount = 0;
    int failureCount = 0;

    for (int i = 0; i < productIds.size(); i++) {
      Long productId = productIds.get(i);
      Integer quantity = quantities.get(i);

      try {
        restoreStock(productId, quantity, reason);
        successCount++;
        logger.debug("Successfully restored stock for product ID {}: quantity {}", productId, quantity);
      } catch (Exception e) {
        failureCount++;
        logger.error("Failed to restore stock for product ID {}: {}", productId, e.getMessage());
      }
    }

    logger.info("Bulk stock restoration completed. Success: {}, Failures: {}", successCount, failureCount);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean checkBulkStockAvailability(List<Long> productIds, List<Integer> quantities) {
    // Checks stock availability for multiple products at once.
    // Returns false immediately if any product does not have enough stock.
    if (productIds.size() != quantities.size()) {
      throw new IllegalArgumentException("Product IDs and quantities lists must have the same size");
    }

    logger.debug("Checking bulk stock availability for {} products", productIds.size());

    for (int i = 0; i < productIds.size(); i++) {
      Long productId = productIds.get(i);
      Integer quantity = quantities.get(i);

      if (!checkStockAvailability(productId, quantity)) {
        logger.warn("Insufficient stock for product ID {}: requested {}", productId, quantity);
        return false;
      }
    }

    logger.debug("Bulk stock availability check passed for all {} products", productIds.size());
    return true;
  }
}