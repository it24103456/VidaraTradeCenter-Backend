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
    Product product = getProductById(productId);
    return product.getStock() != null && product.getStock() >= quantity;
  }

  @Override
  public void reduceStock(Long productId, Integer quantity, String reason) {
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
    return productRepository.findLowStockProducts(Pageable.unpaged()).stream()
        .map(this::toLowStockProductResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<LowStockProductResponse> getOutOfStockProducts() {
    return productRepository.findOutOfStockProducts(Pageable.unpaged()).stream()
        .map(this::toLowStockProductResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<StockAdjustmentResponse> getStockHistory(Long productId) {
    return stockAdjustmentRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
        .map(this::toStockAdjustmentResponse)
        .collect(Collectors.toList());
  }

  // PRIVATE HELPER METHODS

  private Product getProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
  }

  private User getUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
  }

  private Integer getCurrentStock(Product product) {
    return product.getStock() != null ? product.getStock() : 0;
  }

  private void updateProductStock(Product product, Integer newStock) {
    product.setStock(newStock);
    productRepository.save(product);
  }

  private void validateSufficientStock(Product product, Integer currentStock, Integer requestedQuantity) {
    if (currentStock < requestedQuantity) {
      throw new InsufficientStockException(product.getName(), requestedQuantity, currentStock);
    }
  }

  private void validateNonNegativeStock(Integer stock) {
    if (stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative after adjustment");
    }
  }

  private StockAdjustment recordAdjustment(Product product, Integer quantityChange,
      Integer stockBefore, Integer stockAfter,
      StockAdjustmentType type, String reason, User user) {
    StockAdjustment adjustment = new StockAdjustment(product, quantityChange,
        stockBefore, stockAfter, type, reason);
    if (user != null) {
      adjustment.setAdjustedBy(user);
    }
    return stockAdjustmentRepository.save(adjustment);
  }

  private StockAdjustmentResponse toStockAdjustmentResponse(StockAdjustment adjustment) {
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
    return new LowStockProductResponse(
        product.getId(),
        product.getName(),
        product.getSku(),
        product.getStock(),
        product.getLowStockThreshold(),
        product.getStatus().name());
  }
}
