package com.vidara.tradecenter.checkout.service;

import com.vidara.tradecenter.cart.model.Cart;
import com.vidara.tradecenter.cart.model.CartItem;
import com.vidara.tradecenter.cart.model.enums.CartStatus;
import com.vidara.tradecenter.cart.repository.CartRepository;
import com.vidara.tradecenter.checkout.dto.CheckoutRequest;
import com.vidara.tradecenter.checkout.dto.CheckoutResponse;
import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.membership.model.enums.MembershipPlan;
import com.vidara.tradecenter.membership.service.MembershipPricingCalculator;
import com.vidara.tradecenter.membership.service.MembershipService;
import com.vidara.tradecenter.notification.dto.OrderConfirmationEmail;
import com.vidara.tradecenter.notification.event.OrderConfirmedEvent;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.OrderItem;
import com.vidara.tradecenter.order.model.ShippingAddress;
import com.vidara.tradecenter.order.model.enums.PaymentStatus;
import com.vidara.tradecenter.order.repository.OrderRepository;
import com.vidara.tradecenter.product.model.Product;
import com.vidara.tradecenter.product.model.enums.ProductStatus;
import com.vidara.tradecenter.product.repository.ProductRepository;
import com.vidara.tradecenter.user.model.Address;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.AddressRepository;
import com.vidara.tradecenter.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MembershipService membershipService;

    public CheckoutService(CartRepository cartRepository,
            AddressRepository addressRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository,
            ApplicationEventPublisher eventPublisher,
            MembershipService membershipService) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.membershipService = membershipService;
    }

    @Transactional(readOnly = false)
    public CheckoutResponse placeOrder(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No active cart found"));

        List<CartItem> cartItems = cart.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address shippingAddr = addressRepository.findByIdAndUserId(request.getShippingAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getShippingAddressId()));

        BigDecimal subtotal = BigDecimal.ZERO;
        String orderNumber = generateUniqueOrderNumber();
        Order order = new Order(orderNumber, user, BigDecimal.ZERO, BigDecimal.ZERO);
        order.setTax(BigDecimal.ZERO);
        order.setShippingCost(BigDecimal.ZERO);
        order.setPaymentStatus(PaymentStatus.PENDING);

        MembershipPlan activePlan = membershipService.getActivePlan(userId).orElse(null);

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BadRequestException("Product is not available: " + product.getName());
            }

            int quantity = cartItem.getQuantity();
            int availableStock = product.getStock() == null ? 0 : product.getStock();
            if (availableStock < quantity) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }

            BigDecimal retailUnit = MembershipPricingCalculator.retailUnitPrice(product);
            BigDecimal unitPrice = MembershipPricingCalculator.effectiveUnitPrice(retailUnit, quantity, activePlan);
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(totalPrice);

            OrderItem orderItem = new OrderItem(product, product.getName(), quantity, unitPrice, totalPrice);
            order.addItem(orderItem);

            // Decrement stock
            product.setStock(availableStock - quantity);
            productRepository.save(product);
        }

        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal);

        ShippingAddress shippingAddress = new ShippingAddress(
                shippingAddr.getRecipientName(),
                shippingAddr.getPhone(),
                shippingAddr.getStreet(),
                shippingAddr.getCity(),
                shippingAddr.getState() != null ? shippingAddr.getState() : "",
                shippingAddr.getZipCode() != null ? shippingAddr.getZipCode() : "");
        shippingAddress.setCountry(shippingAddr.getCountry());
        order.setShippingDetails(shippingAddress);

        Order saved = orderRepository.save(order);

        cart.setStatus(CartStatus.MERGED_TO_ORDER);
        cartRepository.save(cart);

        // Order confirmation email is sent from PayHereService after successful payment (notify status 2).

        CheckoutResponse response = new CheckoutResponse();
        response.setOrderNumber(saved.getOrderNumber());
        response.setTotalAmount(saved.getTotalAmount());
        return response;
    }

    /**
     * Creates an order for a single product line (used for subscription renewals).
     * Stock is decremented; order confirmation notification is published like normal checkout.
     * Uses a new transaction so each renewal commits even when batch processing subscriptions.
     */
    @Transactional(TxType.REQUIRES_NEW)
    public CheckoutResponse placeSingleItemOrder(Long userId, Long productId, int quantity,
            Long shippingAddressId, BigDecimal unitPrice) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Invalid unit price");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is not available: " + product.getName());
        }

        int availableStock = product.getStock() == null ? 0 : product.getStock();
        if (availableStock < quantity) {
            throw new BadRequestException("Insufficient stock for " + product.getName());
        }

        Address shippingAddr = addressRepository.findByIdAndUserId(shippingAddressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", shippingAddressId));

        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        String orderNumber = generateUniqueOrderNumber();
        Order order = new Order(orderNumber, user, totalPrice, totalPrice);
        order.setTax(BigDecimal.ZERO);
        order.setShippingCost(BigDecimal.ZERO);
        order.setPaymentStatus(PaymentStatus.PENDING);

        OrderItem orderItem = new OrderItem(product, product.getName(), quantity, unitPrice, totalPrice);
        order.addItem(orderItem);

        product.setStock(availableStock - quantity);
        productRepository.save(product);

        order.setSubtotal(totalPrice);
        order.setTotalAmount(totalPrice);

        ShippingAddress shippingAddress = new ShippingAddress(
                shippingAddr.getRecipientName(),
                shippingAddr.getPhone(),
                shippingAddr.getStreet(),
                shippingAddr.getCity(),
                shippingAddr.getState() != null ? shippingAddr.getState() : "",
                shippingAddr.getZipCode() != null ? shippingAddr.getZipCode() : "");
        shippingAddress.setCountry(shippingAddr.getCountry());
        order.setShippingDetails(shippingAddress);

        Order saved = orderRepository.save(order);

        publishOrderConfirmedEvent(saved, user, shippingAddress);

        CheckoutResponse response = new CheckoutResponse();
        response.setOrderNumber(saved.getOrderNumber());
        response.setTotalAmount(saved.getTotalAmount());
        return response;
    }

    /**
     * Called when PayHere server notify reports successful payment ({@code status_code=2}).
     */
    public void publishOrderConfirmationAfterSuccessfulPayment(Order order) {
        String on = order.getOrderNumber();
        if (order.getShippingAddress() == null) {
            log.warn("[ORDER_MAIL] SKIP order={} reason=no shipping address on order entity", on);
            return;
        }
        User user = order.getUser();
        if (user == null) {
            log.warn("[ORDER_MAIL] SKIP order={} reason=no user on order entity", on);
            return;
        }
        log.info("[ORDER_MAIL] START pipeline order={} recipientEmail={}", on, user.getEmail());
        publishOrderConfirmedEvent(order, user, order.getShippingAddress());
    }

    private void publishOrderConfirmedEvent(Order order, User user, ShippingAddress addr) {
        try {
            OrderConfirmationEmail emailData = new OrderConfirmationEmail();
            emailData.setCustomerName(user.getFullName());
            emailData.setCustomerEmail(user.getEmail());
            emailData.setOrderNumber(order.getOrderNumber());
            emailData.setOrderDate(order.getOrderDate());
            emailData.setSubtotal(order.getSubtotal());
            emailData.setTax(order.getTax());
            emailData.setShippingCost(order.getShippingCost());
            emailData.setTotalAmount(order.getTotalAmount());

            List<OrderConfirmationEmail.ItemDetail> itemDetails = order.getItems().stream()
                    .map(item -> new OrderConfirmationEmail.ItemDetail(
                            item.getProductName(), item.getQuantity(),
                            item.getUnitPrice(), item.getTotalPrice()))
                    .collect(Collectors.toList());
            emailData.setItems(itemDetails);

            String addressStr = addr.getFullName() + ", " + addr.getAddressLine1()
                    + ", " + addr.getCity()
                    + (addr.getState() != null && !addr.getState().isEmpty() ? ", " + addr.getState() : "")
                    + (addr.getPostalCode() != null && !addr.getPostalCode().isEmpty() ? " " + addr.getPostalCode() : "")
                    + ", " + addr.getCountry();
            emailData.setShippingAddress(addressStr);

            eventPublisher.publishEvent(new OrderConfirmedEvent(this, emailData));
            log.info("[ORDER_MAIL] Event published order={} Spring will send SMTP async to {}",
                    order.getOrderNumber(), user.getEmail());
        } catch (Exception e) {
            log.error("[ORDER_MAIL] FAILED to publish OrderConfirmedEvent order={}: {}",
                    order.getOrderNumber(), e.getMessage(), e);
        }
    }

    private BigDecimal resolveUnitPrice(Product product) {
        BigDecimal basePrice = product.getBasePrice();
        BigDecimal salePrice = product.getSalePrice();
        if (salePrice != null && basePrice != null && salePrice.compareTo(basePrice) < 0) {
            return salePrice;
        }
        return basePrice;
    }

    private String generateUniqueOrderNumber() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = "OD" + System.currentTimeMillis() + "-" + randomSuffix();
            if (!orderRepository.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }
        throw new BadRequestException("Could not generate a unique order number");
    }

    private String randomSuffix() {
        String u = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return u.substring(0, Math.min(8, u.length()));
    }
}
