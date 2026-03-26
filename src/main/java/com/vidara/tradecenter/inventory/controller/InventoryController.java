package com.vidara.tradecenter.inventory.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.inventory.dto.request.StockAdjustmentRequest;
import com.vidara.tradecenter.inventory.dto.response.LowStockProductResponse;
import com.vidara.tradecenter.inventory.dto.response.StockAdjustmentResponse;
import com.vidara.tradecenter.inventory.service.InventoryService;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@Tag(name = "Inventory Management", description = "Admin inventory and stock management APIs")
public class InventoryController {

  private final InventoryService inventoryService;

  public InventoryController(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @PostMapping("/adjust")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Adjust product stock", description = "Manually adjust stock quantity for a product")
  public ResponseEntity<ApiResponse<StockAdjustmentResponse>> adjustStock(
      @CurrentUser CustomUserDetails currentUser,
      @Valid @RequestBody StockAdjustmentRequest request) {
    StockAdjustmentResponse response = inventoryService.adjustStock(request, currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", response));
  }

  @GetMapping("/low-stock")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get low stock products", description = "Get list of products with stock below threshold")
  public ResponseEntity<ApiResponse<List<LowStockProductResponse>>> getLowStockProducts() {
    List<LowStockProductResponse> products = inventoryService.getLowStockProducts();
    return ResponseEntity.ok(ApiResponse.success("Low stock products retrieved successfully", products));
  }

  @GetMapping("/out-of-stock")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get out of stock products", description = "Get list of products that are out of stock")
  public ResponseEntity<ApiResponse<List<LowStockProductResponse>>> getOutOfStockProducts() {
    List<LowStockProductResponse> products = inventoryService.getOutOfStockProducts();
    return ResponseEntity.ok(ApiResponse.success("Out of stock products retrieved successfully", products));
  }

  @GetMapping("/history/{productId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get stock history", description = "Get stock adjustment history for a product")
  public ResponseEntity<ApiResponse<List<StockAdjustmentResponse>>> getStockHistory(
      @PathVariable Long productId) {
    List<StockAdjustmentResponse> history = inventoryService.getStockHistory(productId);
    return ResponseEntity.ok(ApiResponse.success("Stock history retrieved successfully", history));
  }
}
