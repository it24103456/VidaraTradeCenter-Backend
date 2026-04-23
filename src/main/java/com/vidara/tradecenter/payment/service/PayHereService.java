package com.vidara.tradecenter.payment.service;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.order.exception.OrderNotFoundException;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.OrderItem;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import com.vidara.tradecenter.order.model.enums.PaymentStatus;
import com.vidara.tradecenter.membership.model.MembershipPaymentIntent;
import com.vidara.tradecenter.checkout.service.CheckoutService;
import com.vidara.tradecenter.membership.service.MembershipCheckoutService;
import com.vidara.tradecenter.order.repository.OrderRepository;
import com.vidara.tradecenter.payment.config.PayHereProperties;
import com.vidara.tradecenter.payment.dto.PaymentInitiateResponse;
import com.vidara.tradecenter.payment.exception.PaymentException;
import com.vidara.tradecenter.payment.model.PayHereNotifyReceipt;
import com.vidara.tradecenter.payment.repository.PayHereNotifyReceiptRepository;
import com.vidara.tradecenter.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final PayHereNotifyReceiptRepository notifyReceiptRepository;
    private final MembershipCheckoutService membershipCheckoutService;
    private final CheckoutService checkoutService;

    public PayHereService(PayHereProperties props,
                          OrderRepository orderRepository,
                          PayHereNotifyReceiptRepository notifyReceiptRepository,
                          MembershipCheckoutService membershipCheckoutService,
                          CheckoutService checkoutService) {
        this.props = props;
        this.orderRepository = orderRepository;
        this.notifyReceiptRepository = notifyReceiptRepository;
        this.membershipCheckoutService = membershipCheckoutService;
        this.checkoutService = checkoutService;
    }

    /**
     * Sandbox-only: customer finished PayHere in the browser — mark order paid and send confirmation email
     * (for local dev when server notify is unreachable).
     */
    @Transactional
    public void reconcileSandboxOrderIfPending(Long userId, String orderNumber) {
        if (!props.isSandbox() || !props.isOrderSandboxReconcileEnabled()) {
            throw new BadRequestException(
                    "Sandbox order reconcile is disabled. For local dev set payhere.order-sandbox-reconcile-enabled=true "
                            + "or configure payhere.notify-base-url to a public URL PayHere can reach.");
        }
        if (MembershipCheckoutService.isMembershipOrderId(orderNumber)) {
            throw new BadRequestException("Use membership reconcile for MS orders");
        }
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("orderNumber", orderNumber));
        if (!order.getUser().getId().equals(userId)) {
            throw new PaymentException("Order does not belong to this user", orderNumber);
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            log.info("[ORDER_MAIL] Sandbox reconcile skipped order={} paymentStatus={} (no email trigger; already finalized)",
                    orderNumber, order.getPaymentStatus());
            return;
        }
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setOrderStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("[ORDER_MAIL] Sandbox reconcile marking PAID order={} then running confirmation pipeline", orderNumber);
        checkoutService.publishOrderConfirmationAfterSuccessfulPayment(order);
        log.warn(
                "SANDBOX: Order {} marked paid via client callback (send confirmation email). Disable payhere.order-sandbox-reconcile-enabled for live.",
                orderNumber);
    }

    @Transactional
    public PaymentInitiateResponse initiatePayment(Long userId, String orderNumber, String serverBaseUrl) {
        if (MembershipCheckoutService.isMembershipOrderId(orderNumber)) {
            return initiateMembershipPayment(userId, orderNumber, serverBaseUrl);
        }

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("orderNumber", orderNumber));

        if (!order.getUser().getId().equals(userId)) {
            throw new PaymentException("Order does not belong to this user", orderNumber);
        }
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new PaymentException("Payment already completed for this order", orderNumber);
        }

        User user = order.getUser();
        BigDecimal amount = order.getTotalAmount();
        String formattedAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();

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
        response.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
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
        response.setNotifyUrl(buildNotifyUrl(serverBaseUrl));

        return response;
    }

    private PaymentInitiateResponse initiateMembershipPayment(Long userId, String orderNumber, String serverBaseUrl) {
        MembershipPaymentIntent intent = membershipCheckoutService.loadForPaymentInitiate(userId, orderNumber);
        User user = intent.getUser();
        BigDecimal amount = intent.getAmount();
        String formattedAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();

        String hash = generateCheckoutHash(
                props.getMerchantId(),
                orderNumber,
                formattedAmount,
                CURRENCY,
                props.getMerchantSecret()
        );

        String itemsSummary = membershipCheckoutService.buildItemsSummary(intent);

        PaymentInitiateResponse response = new PaymentInitiateResponse();
        response.setSandbox(props.isSandbox());
        response.setMerchantId(props.getMerchantId());
        response.setOrderId(orderNumber);
        response.setItems(itemsSummary);
        response.setCurrency(CURRENCY);
        response.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        response.setHash(hash);

        response.setFirstName(user.getFirstName() != null ? user.getFirstName() : "");
        response.setLastName(user.getLastName() != null ? user.getLastName() : "");
        response.setEmail(user.getEmail() != null ? user.getEmail() : "");
        response.setPhone(user.getPhone() != null ? user.getPhone() : "");
        response.setAddress("");
        response.setCity("");
        response.setCountry("Sri Lanka");

        response.setReturnUrl(props.getFrontendUrl() + "/payment/return?order=" + orderNumber);
        response.setCancelUrl(props.getFrontendUrl() + "/payment/cancel?order=" + orderNumber);
        response.setNotifyUrl(buildNotifyUrl(serverBaseUrl));

        return response;
    }

    private String buildNotifyUrl(String requestDerivedBaseUrl) {
        String base;
        if (props.getNotifyBaseUrl() != null && !props.getNotifyBaseUrl().isBlank()) {
            base = props.getNotifyBaseUrl().trim().replaceAll("/+$", "");
        } else {
            base = requestDerivedBaseUrl.trim().replaceAll("/+$", "");
        }
        String notifyUrl = base + "/api/payment/notify";
        log.info("PayHere notify_url={}", notifyUrl);
        return notifyUrl;
    }

    @Transactional
    public void handleNotification(HttpServletRequest request) {
        String merchantId = param(request, "merchant_id");
        String orderId = param(request, "order_id");
        String paymentId = param(request, "payment_id").trim();
        String payhereAmount = param(request, "payhere_amount");
        String payhereCurrency = param(request, "payhere_currency");
        String statusCodeRaw = param(request, "status_code");
        String md5sig = param(request, "md5sig").trim();

        log.info("PayHere notification received — order_id={}, status_code={}, payment_id={}",
                orderId, statusCodeRaw, paymentId);

        String localSig = generateNotifyHash(
                merchantId,
                orderId,
                payhereAmount,
                payhereCurrency,
                statusCodeRaw,
                props.getMerchantSecret()
        );

        if (!localSig.equalsIgnoreCase(md5sig)) {
            log.warn("PayHere notify rejected (possible fraud): invalid MD5 signature for order_id={}", orderId);
            throw new BadRequestException("Invalid payment signature");
        }

        if (!props.getMerchantId().equals(merchantId)) {
            log.warn("PayHere notify rejected (possible fraud): merchant_id mismatch for order_id={}", orderId);
            throw new BadRequestException("Merchant ID mismatch");
        }

        int code;
        try {
            code = Integer.parseInt(statusCodeRaw.trim());
        } catch (NumberFormatException e) {
            log.warn("PayHere notify rejected: invalid status_code '{}' for order_id={}", statusCodeRaw, orderId);
            throw new BadRequestException("Invalid status_code");
        }

        if (MembershipCheckoutService.isMembershipOrderId(orderId)) {
            membershipCheckoutService.handlePayHereNotification(orderId, paymentId, payhereAmount, payhereCurrency, code);
            return;
        }

        Order order = orderRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> {
                    log.warn("PayHere notification for unknown order: {}", orderId);
                    return new OrderNotFoundException("orderNumber", orderId);
                });

        verifyCurrency(payhereCurrency, orderId);
        verifyAmount(payhereAmount, order, orderId);

        if (isDuplicateNotification(paymentId, code, order)) {
            log.info("PayHere duplicate notification ignored — order_id={}, payment_id={}, status_code={}",
                    orderId, paymentId.isEmpty() ? "(empty)" : paymentId, code);
            return;
        }

        PaymentStatus paymentStatusBefore = order.getPaymentStatus();
        OrderStatus orderStatusBefore = order.getOrderStatus();

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

        if (!paymentId.isEmpty()) {
            try {
                notifyReceiptRepository.save(new PayHereNotifyReceipt(orderId, paymentId, code));
            } catch (DataIntegrityViolationException e) {
                log.info("PayHere duplicate notification (race on payment_id): order_id={}, payment_id={}",
                        orderId, paymentId);
            }
        }

        boolean newlyPaid = code == 2
                && (paymentStatusBefore != PaymentStatus.COMPLETED || orderStatusBefore != OrderStatus.PAID);
        if (newlyPaid) {
            log.info("[ORDER_MAIL] PayHere notify: newly paid order={} triggering confirmation pipeline", orderId);
            checkoutService.publishOrderConfirmationAfterSuccessfulPayment(order);
        }
    }

    private void verifyCurrency(String payhereCurrency, String orderId) {
        if (payhereCurrency == null || payhereCurrency.isBlank()) {
            log.warn("PayHere notify rejected: missing payhere_currency for order_id={}", orderId);
            throw new BadRequestException("Missing payhere_currency");
        }
        if (!CURRENCY.equalsIgnoreCase(payhereCurrency.trim())) {
            log.warn("PayHere notify rejected: currency mismatch for order_id={} (expected {}, got {})",
                    orderId, CURRENCY, payhereCurrency);
            throw new BadRequestException("Currency mismatch");
        }
    }

    private void verifyAmount(String payhereAmount, Order order, String orderId) {
        if (payhereAmount == null || payhereAmount.isBlank()) {
            log.warn("PayHere notify rejected: missing payhere_amount for order_id={}", orderId);
            throw new BadRequestException("Missing payhere_amount");
        }
        BigDecimal notified;
        try {
            notified = new BigDecimal(payhereAmount.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("PayHere notify rejected: invalid payhere_amount '{}' for order_id={}", payhereAmount, orderId);
            throw new BadRequestException("Invalid payhere_amount");
        }
        BigDecimal expected = order.getTotalAmount().setScale(2, RoundingMode.HALF_UP);
        if (expected.compareTo(notified) != 0) {
            log.warn("PayHere notify rejected: amount mismatch for order_id={} (expected {}, got {})",
                    orderId, expected, notified);
            throw new BadRequestException("Payment amount mismatch");
        }
    }

    /**
     * Duplicate if we already stored this payment_id, or success (code 2) retried without payment_id
     * while the order is already PAID/COMPLETED.
     */
    private boolean isDuplicateNotification(String paymentId, int code, Order order) {
        if (!paymentId.isEmpty()) {
            if (notifyReceiptRepository.existsByPaymentId(paymentId)) {
                return true;
            }
            return false;
        }
        return code == 2
                && order.getOrderStatus() == OrderStatus.PAID
                && order.getPaymentStatus() == PaymentStatus.COMPLETED;
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
