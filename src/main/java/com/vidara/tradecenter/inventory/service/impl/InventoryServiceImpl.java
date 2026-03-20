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
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

    return product.getStock() != null && product.getStock() >= quantity;
  }

  @Override
  public void reduceStock(Long productId, Integer quantity, String reason) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

    Integer currentStock = product.getStock() != null ? product.getStock() : 0;

    if (currentStock < quantity) {
      throw new InsufficientStockException(product.getName(), quantity, currentStock);
    }

    Integer stockBefore = currentStock;
    Integer stockAfter = currentStock - quantity;

    product.setStock(stockAfter);
    productRepository.save(product);

    // Record adjustment
    StockAdjustment adjustment = new StockAdjustment(
        product,
        -quantity,
        stockBefore,
        stockAfter,
        StockAdjustmentType.ORDER_PLACED,
        reason);
    stockAdjustmentRepository.save(adjustment);

    logger.info("Stock reduced for product {}: {} -> {} (quantity: {})",
        product.getSku(), stockBefore, stockAfter, quantity);
  }

  @Override
  public void restoreStock(Long productId, Integer quantity, String reason) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

    Integer currentStock = product.getStock() != null ? product.getStock() : 0;
    Integer stockBefore = currentStock;
    Integer stockAfter = currentStock + quantity;

    product.setStock(stockAfter);
    productRepository.save(product);

    // Record adjustment
    StockAdjustment adjustment = new StockAdjustment(
        product,
        quantity,
        stockBefore,
        stockAfter,
        StockAdjustmentType.ORDER_CANCELLED,
        reason);
    stockAdjustmentRepository.save(adjustment);

    logger.info("Stock restored for product {}: {} -> {} (quantity: {})",
        product.getSku(), stockBefore, stockAfter, quantity);
  }

  @Override
  public StockAdjustmentResponse adjustStock(StockAdjustmentRequest request, Long userId) {
    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    Integer currentStock = product.getStock() != null ? product.getStock() : 0;
    Integer stockBefore = currentStock;
    Integer stockAfter = currentStock + request.getQuantityChange();

    if (stockAfter < 0) {
      throw new IllegalArgumentException("Stock cannot be negative after adjustment");
    }

    product.setStock(stockAfter);
    productRepository.save(product);

    // Record adjustment
    StockAdjustment adjustment = new StockAdjustment(
        product,
        request.getQuantityChange(),
        stockBefore,
        stockAfter,
        StockAdjustmentType.MANUAL_ADJUSTMENT,
        request.getReason());
    adjustment.setAdjustedBy(user);
    adjustment = stockAdjustmentRepository.save(adjustment);

    logger.info("Manual stock adjustment for product {} by user {}: {} -> {} (change: {})",
        product.getSku(), user.getEmail(), stockBefore, stockAfter, request.getQuantityChange());

    return toStockAdjustmentResponse(adjustment);
  }

  @Override
  public void recordStockAdjustment(Long productId, Integer quantityChange,
      StockAdjustmentType type, String reason, Long userId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

    Integer currentStock = product.getStock() != null ? product.getStock() : 0;
    Integer stockBefore = currentStock;
    Integer stockAfter = currentStock + quantityChange;

    product.setStock(stockAfter);
    productRepository.save(product);

    StockAdjustment adjustment = new StockAdjustment(
        product,
        quantityChange,
        stockBefore,
        stockAfter,
        type,
        reason);

    if (userId != null) {
      User user = userRepository.findById(userId).orElse(null);
      adjustment.setAdjustedBy(user);
    }

    stockAdjustmentRepository.save(adjustment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<LowStockProductResponse> getLowStockProducts() {
    List<Product> products = productRepository.findAll();

    return products.stream()
        .filter(Product::isLowStock)
        .map(this::toLowStockProductResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<LowStockProductResponse> getOutOfStockProducts() {
    List<Product> products = productRepository.findAll();

    return products.stream()
        .filter(Product::isOutOfStock)
        .map(this::toLowStockProductResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<StockAdjustmentResponse> getStockHistory(Long productId) {
    List<StockAdjustment> adjustments = stockAdjustmentRepository
        .findByProductIdOrderByCreatedAtDesc(productId);

    return adjustments.stream()
        .map(this::toStockAdjustmentResponse)
        .collect(Collectors.toList());
  }

  // PRIVATE HELPER METHODS

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
