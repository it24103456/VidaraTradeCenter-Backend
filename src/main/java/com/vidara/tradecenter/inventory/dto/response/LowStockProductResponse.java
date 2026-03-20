package com.vidara.tradecenter.inventory.dto.response;

public class LowStockProductResponse {

  private Long id;
  private String name;
  private String sku;
  private Integer currentStock;
  private Integer lowStockThreshold;
  private String status;

  // CONSTRUCTORS

  public LowStockProductResponse() {
  }

  public LowStockProductResponse(Long id, String name, String sku, Integer currentStock,
      Integer lowStockThreshold, String status) {
    this.id = id;
    this.name = name;
    this.sku = sku;
    this.currentStock = currentStock;
    this.lowStockThreshold = lowStockThreshold;
    this.status = status;
  }

  // GETTERS AND SETTERS

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public Integer getCurrentStock() {
    return currentStock;
  }

  public void setCurrentStock(Integer currentStock) {
    this.currentStock = currentStock;
  }

  public Integer getLowStockThreshold() {
    return lowStockThreshold;
  }

  public void setLowStockThreshold(Integer lowStockThreshold) {
    this.lowStockThreshold = lowStockThreshold;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
