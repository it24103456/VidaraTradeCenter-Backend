package com.vidara.tradecenter.inventory.service;

import com.vidara.tradecenter.inventory.dto.request.StockAdjustmentRequest;
import com.vidara.tradecenter.inventory.dto.response.LowStockProductResponse;
import com.vidara.tradecenter.inventory.dto.response.StockAdjustmentResponse;
import com.vidara.tradecenter.inventory.model.enums.StockAdjustmentType;

import java.util.List;

public interface InventoryService {

  /**
   * Check if sufficient stock is available for a product
   */
  boolean checkStockAvailability(Long productId, Integer quantity);

  /**
   * Reduce stock when an order is placed
   */
  void reduceStock(Long productId, Integer quantity, String reason);

  /**
   * Restore stock when an order is cancelled
   */
  void restoreStock(Long productId, Integer quantity, String reason);

  /**
   * Manually adjust stock (admin operation)
   */
  StockAdjustmentResponse adjustStock(StockAdjustmentRequest request, Long userId);

  /**
   * Record stock adjustment in history
   */
  void recordStockAdjustment(Long productId, Integer quantityChange,
      StockAdjustmentType type, String reason, Long userId);

  /**
   * Get products with low stock
   */
  List<LowStockProductResponse> getLowStockProducts();

  /**
   * Get products that are out of stock
   */
  List<LowStockProductResponse> getOutOfStockProducts();

  /**
   * Get stock adjustment history for a product
   */
  List<StockAdjustmentResponse> getStockHistory(Long productId);

  /**
   * Bulk reduce stock for multiple products (order processing)
   */
  void bulkReduceStock(List<Long> productIds, List<Integer> quantities, String reason);

  /**
   * Bulk restore stock for multiple products (order cancellation)
   */
  void bulkRestoreStock(List<Long> productIds, List<Integer> quantities, String reason);

  /**
   * Check stock availability for multiple products at once
   */
  boolean checkBulkStockAvailability(List<Long> productIds, List<Integer> quantities);
}
