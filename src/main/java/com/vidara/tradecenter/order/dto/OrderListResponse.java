package com.vidara.tradecenter.order.dto;

import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.ShippingAddress;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderListResponse {

    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime orderDate;
    private int itemCount;
    private List<ItemInfo> items;
    private ShippingInfo shippingAddress;


    // INNER DTOs

    public static class ItemInfo {

        private Long id;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public ItemInfo() {
        }

        public ItemInfo(Long id, String productName, Integer quantity,
                        BigDecimal unitPrice, BigDecimal totalPrice) {
            this.id = id;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }

    public static class ShippingInfo {

        private String fullName;
        private String phone;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        public ShippingInfo() {
        }

        public ShippingInfo(String fullName, String phone, String addressLine1,
                            String addressLine2, String city, String state,
                            String postalCode, String country) {
            this.fullName = fullName;
            this.phone = phone;
            this.addressLine1 = addressLine1;
            this.addressLine2 = addressLine2;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = country;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddressLine1() {
            return addressLine1;
        }

        public void setAddressLine1(String addressLine1) {
            this.addressLine1 = addressLine1;
        }

        public String getAddressLine2() {
            return addressLine2;
        }

        public void setAddressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }


    // CONSTRUCTORS

    public OrderListResponse() {
    }


    // FACTORY METHODS

    public static OrderListResponse fromEntity(Order order) {
        OrderListResponse response = new OrderListResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerName(order.getUser().getFullName());
        response.setCustomerEmail(order.getUser().getEmail());
        response.setSubtotal(order.getSubtotal());
        response.setTax(order.getTax());
        response.setShippingCost(order.getShippingCost());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderStatus(order.getOrderStatus().name());
        response.setPaymentStatus(order.getPaymentStatus().name());
        response.setOrderDate(order.getOrderDate());
        response.setItemCount(order.getItemCount());
        return response;
    }

    public static OrderListResponse fromEntityDetailed(Order order) {
        OrderListResponse response = fromEntity(order);

        if (order.getItems() != null) {
            List<ItemInfo> itemInfos = order.getItems().stream()
                    .map(item -> new ItemInfo(
                            item.getId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotalPrice()))
                    .toList();
            response.setItems(itemInfos);
        }

        if (order.getShippingAddress() != null) {
            ShippingAddress addr = order.getShippingAddress();
            response.setShippingAddress(new ShippingInfo(
                    addr.getFullName(),
                    addr.getPhone(),
                    addr.getAddressLine1(),
                    addr.getAddressLine2(),
                    addr.getCity(),
                    addr.getState(),
                    addr.getPostalCode(),
                    addr.getCountry()));
        }

        return response;
    }


    // GETTERS AND SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public List<ItemInfo> getItems() {
        return items;
    }

    public void setItems(List<ItemInfo> items) {
        this.items = items;
    }

    public ShippingInfo getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingInfo shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}