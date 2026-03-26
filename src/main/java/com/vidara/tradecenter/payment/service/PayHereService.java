package com.vidara.tradecenter.payment.service;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.OrderItem;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import com.vidara.tradecenter.order.model.enums.PaymentStatus;
import com.vidara.tradecenter.order.repository.OrderRepository;
import com.vidara.tradecenter.payment.config.PayHereProperties;
import com.vidara.tradecenter.payment.dto.PaymentInitiateResponse;
import com.vidara.tradecenter.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

@Service
public class PayHereService {

    private static final Logger log = LoggerFactory.getLogger(PayHereService.class);
    private static final String CURRENCY = "LKR";

    private final PayHereProperties props;
    private final OrderRepository orderRepository;

    public PayHereService(PayHereProperties props, OrderRepository orderRepository) {
        this.props = props;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentInitiateResponse initiatePayment(Long userId, String orderNumber, String serverBaseUrl) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment already completed for this order");
        }

        User user = order.getUser();
        BigDecimal amount = order.getTotalAmount();
        String formattedAmount = amount.setScale(2).toPlainString();

        String hash = generateCheckoutHash(
                props.getMerchantId(),
                orderNumber,
                formattedAmount,
                CURRENCY,
                props.getMerchantSecret()
        );

        String itemsSummary = order.getItems().stream()
                .map(OrderItem::getProductName)
                .collect(Collectors.joining(", "));
        if (itemsSummary.length() > 200) {
            itemsSummary = itemsSummary.substring(0, 197) + "...";
        }

        String shippingCity = "";
        String shippingCountry = "Sri Lanka";
        String shippingAddress = "";
        if (order.getShippingAddress() != null) {
            shippingAddress = order.getShippingAddress().getAddressLine1() != null
                    ? order.getShippingAddress().getAddressLine1() : "";
            shippingCity = order.getShippingAddress().getCity() != null
                    ? order.getShippingAddress().getCity() : "";
            shippingCountry = order.getShippingAddress().getCountry() != null
                    ? order.getShippingAddress().getCountry() : "Sri Lanka";
        }

        PaymentInitiateResponse response = new PaymentInitiateResponse();
        response.setSandbox(props.isSandbox());
        response.setMerchantId(props.getMerchantId());
        response.setOrderId(orderNumber);
        response.setItems(itemsSummary);
        response.setCurrency(CURRENCY);
        response.setAmount(amount.setScale(2));
        response.setHash(hash);

        response.setFirstName(user.getFirstName() != null ? user.getFirstName() : "");
        response.setLastName(user.getLastName() != null ? user.getLastName() : "");
        response.setEmail(user.getEmail() != null ? user.getEmail() : "");
        response.setPhone(user.getPhone() != null ? user.getPhone() : "");
        response.setAddress(shippingAddress);
        response.setCity(shippingCity);
        response.setCountry(shippingCountry);

        response.setReturnUrl(props.getFrontendUrl() + "/payment/return?order=" + orderNumber);
        response.setCancelUrl(props.getFrontendUrl() + "/payment/cancel?order=" + orderNumber);
        response.setNotifyUrl(serverBaseUrl + "/api/payment/notify");

        return response;
    }

    @Transactional
    public void handleNotification(HttpServletRequest request) {
        String merchantId = param(request, "merchant_id");
        String orderId = param(request, "order_id");
        String paymentId = param(request, "payment_id");
        String payhereAmount = param(request, "payhere_amount");
        String payhereCurrency = param(request, "payhere_currency");
        String statusCode = param(request, "status_code");
        String md5sig = param(request, "md5sig");

        log.info("PayHere notification received — order_id={}, status_code={}, payment_id={}",
                orderId, statusCode, paymentId);

        String localSig = generateNotifyHash(
                merchantId,
                orderId,
                payhereAmount,
                payhereCurrency,
                statusCode,
                props.getMerchantSecret()
        );

        if (!localSig.equals(md5sig)) {
            log.warn("PayHere MD5 signature mismatch for order_id={}", orderId);
            throw new BadRequestException("Invalid payment signature");
        }

        if (!props.getMerchantId().equals(merchantId)) {
            log.warn("Merchant ID mismatch: expected={}, received={}", props.getMerchantId(), merchantId);
            throw new BadRequestException("Merchant ID mismatch");
        }

        Order order = orderRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> {
                    log.warn("PayHere notification for unknown order: {}", orderId);
                    return new ResourceNotFoundException("Order", "orderNumber", orderId);
                });

        int code = Integer.parseInt(statusCode);

        switch (code) {
            case 2 -> {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
                order.setOrderStatus(OrderStatus.PAID);
                log.info("Payment SUCCESS for order {}, paymentId={}", orderId, paymentId);
            }
            case 0 -> {
                order.setPaymentStatus(PaymentStatus.PENDING);
                log.info("Payment PENDING for order {}", orderId);
            }
            case -1, -2 -> {
                order.setPaymentStatus(PaymentStatus.FAILED);
                log.info("Payment FAILED/CANCELED for order {}, code={}", orderId, code);
            }
            case -3 -> {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                order.setOrderStatus(OrderStatus.CANCELLED);
                log.info("Payment CHARGEDBACK for order {}", orderId);
            }
            default -> log.warn("Unknown PayHere status code {} for order {}", code, orderId);
        }

        orderRepository.save(order);
    }

    private String generateCheckoutHash(String merchantId, String orderId,
                                        String amount, String currency,
                                        String merchantSecret) {
        String secretHash = md5(merchantSecret).toUpperCase();
        return md5(merchantId + orderId + amount + currency + secretHash).toUpperCase();
    }

    private String generateNotifyHash(String merchantId, String orderId,
                                      String amount, String currency,
                                      String statusCode, String merchantSecret) {
        String secretHash = md5(merchantSecret).toUpperCase();
        return md5(merchantId + orderId + amount + currency + statusCode + secretHash).toUpperCase();
    }

    private String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    private String param(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value != null ? value : "";
    }
}
