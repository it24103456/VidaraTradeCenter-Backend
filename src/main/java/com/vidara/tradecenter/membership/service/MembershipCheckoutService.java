package com.vidara.tradecenter.membership.service;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.membership.dto.MembershipCheckoutResponse;
import com.vidara.tradecenter.membership.dto.SubscribeMembershipRequest;
import com.vidara.tradecenter.membership.model.MembershipPaymentIntent;
import com.vidara.tradecenter.membership.model.enums.MembershipIntentStatus;
import com.vidara.tradecenter.membership.repository.MembershipPaymentIntentRepository;
import com.vidara.tradecenter.payment.config.PayHereProperties;
import com.vidara.tradecenter.payment.model.PayHereNotifyReceipt;
import com.vidara.tradecenter.payment.repository.PayHereNotifyReceiptRepository;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Creates PayHere-backed membership checkouts (order numbers prefixed with {@code MS}) and
 * completes {@link com.vidara.tradecenter.membership.model.UserMembership} only after a successful notify.
 */
@Service
public class MembershipCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(MembershipCheckoutService.class);
    private static final String ORDER_PREFIX = "MS";

    private final MembershipPaymentIntentRepository intentRepository;
    private final UserRepository userRepository;
    private final PayHereNotifyReceiptRepository notifyReceiptRepository;
    private final MembershipService membershipService;
    private final PayHereProperties payHereProperties;

    public MembershipCheckoutService(MembershipPaymentIntentRepository intentRepository,
            UserRepository userRepository,
            PayHereNotifyReceiptRepository notifyReceiptRepository,
            MembershipService membershipService,
            PayHereProperties payHereProperties) {
        this.intentRepository = intentRepository;
        this.userRepository = userRepository;
        this.notifyReceiptRepository = notifyReceiptRepository;
        this.membershipService = membershipService;
        this.payHereProperties = payHereProperties;
    }

    public static boolean isMembershipOrderId(String orderId) {
        return orderId != null && orderId.startsWith(ORDER_PREFIX);
    }

    @Transactional(readOnly = false)
    public MembershipCheckoutResponse createCheckout(Long userId, SubscribeMembershipRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        BigDecimal amount = MembershipPlanCatalog.displayPrice(request.getPlan(), request.getBillingPeriod())
                .setScale(2, RoundingMode.HALF_UP);

        String orderNumber = generateUniqueOrderNumber();
        MembershipPaymentIntent intent = new MembershipPaymentIntent();
        intent.setOrderNumber(orderNumber);
        intent.setUser(user);
        intent.setPlan(request.getPlan());
        intent.setBillingPeriod(request.getBillingPeriod());
        intent.setAmount(amount);
        intent.setStatus(MembershipIntentStatus.PENDING);
        intentRepository.save(intent);

        MembershipCheckoutResponse r = new MembershipCheckoutResponse();
        r.setOrderNumber(orderNumber);
        r.setAmount(amount);
        r.setPlan(request.getPlan());
        r.setBillingPeriod(request.getBillingPeriod());
        return r;
    }

    @Transactional(readOnly = true)
    public MembershipPaymentIntent loadForPaymentInitiate(Long userId, String orderNumber) {
        MembershipPaymentIntent intent = intentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Membership checkout", "orderNumber", orderNumber));
        if (!intent.getUser().getId().equals(userId)) {
            throw new BadRequestException("Checkout does not belong to this user");
        }
        if (intent.getStatus() != MembershipIntentStatus.PENDING) {
            throw new BadRequestException("This membership checkout is no longer valid for payment");
        }
        return intent;
    }

    /**
     * Sandbox-only escape hatch when PayHere cannot reach {@code notify_url} (e.g. localhost).
     * Live payments must rely on the server notify callback; keep this disabled in production.
     */
    @Transactional(readOnly = false)
    public void reconcileSandboxMembershipIfPending(Long userId, String orderNumber) {
        if (!payHereProperties.isSandbox() || !payHereProperties.isMembershipSandboxReconcileEnabled()) {
            throw new BadRequestException(
                    "Sandbox membership reconcile is disabled. For local dev set payhere.membership-sandbox-reconcile-enabled=true "
                            + "or configure payhere.notify-base-url to a public URL PayHere can reach.");
        }
        if (!isMembershipOrderId(orderNumber)) {
            throw new BadRequestException("Invalid membership order number");
        }
        MembershipPaymentIntent intent = intentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Membership checkout", "orderNumber", orderNumber));
        if (!intent.getUser().getId().equals(userId)) {
            throw new BadRequestException("Checkout does not belong to this user");
        }
        if (intent.getStatus() != MembershipIntentStatus.PENDING) {
            log.debug("Sandbox reconcile skipped — intent {} already {}", orderNumber, intent.getStatus());
            return;
        }
        membershipService.activatePaidSubscription(intent.getUser().getId(), intent.getPlan(), intent.getBillingPeriod());
        intent.setStatus(MembershipIntentStatus.PAID);
        intentRepository.save(intent);
        log.warn(
                "SANDBOX: Membership {} activated via client reconcile (PayHere notify was likely unreachable). Disable payhere.membership-sandbox-reconcile-enabled in real environments.",
                orderNumber);
    }

    public String buildItemsSummary(MembershipPaymentIntent intent) {
        String planTitle = switch (intent.getPlan()) {
            case STARTER -> "Starter";
            case PROFESSIONAL -> "Professional";
            case ENTERPRISE -> "Enterprise";
        };
        String period = intent.getBillingPeriod().name().toLowerCase().replace('_', ' ');
        String raw = "Vidara membership — " + planTitle + " (" + period + ")";
        if (raw.length() > 200) {
            return raw.substring(0, 197) + "...";
        }
        return raw;
    }

    @Transactional(readOnly = false)
    public void handlePayHereNotification(
            String orderId,
            String paymentId,
            String payhereAmount,
            String payhereCurrency,
            int statusCode) {

        MembershipPaymentIntent intent = intentRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> {
                    log.warn("PayHere membership notify: unknown order_id={}", orderId);
                    return new ResourceNotFoundException("Membership checkout", "orderNumber", orderId);
                });

        verifyCurrency(payhereCurrency, orderId);
        verifyAmount(payhereAmount, intent, orderId);

        if (isDuplicateNotification(paymentId, statusCode, intent)) {
            log.info("PayHere duplicate membership notification ignored — order_id={}, payment_id={}, status_code={}",
                    orderId, paymentId.isEmpty() ? "(empty)" : paymentId, statusCode);
            return;
        }

        switch (statusCode) {
            case 2 -> {
                if (intent.getStatus() == MembershipIntentStatus.PENDING) {
                    membershipService.activatePaidSubscription(intent.getUser().getId(), intent.getPlan(),
                            intent.getBillingPeriod());
                    intent.setStatus(MembershipIntentStatus.PAID);
                    intentRepository.save(intent);
                    log.info("Membership payment SUCCESS for {}, userId={}", orderId, intent.getUser().getId());
                }
            }
            case 0 -> log.info("Membership payment PENDING notify for {}", orderId);
            case -1, -2 -> {
                if (intent.getStatus() == MembershipIntentStatus.PENDING) {
                    intent.setStatus(MembershipIntentStatus.FAILED);
                    intentRepository.save(intent);
                }
                log.info("Membership payment FAILED/CANCELED for {}, code={}", orderId, statusCode);
            }
            case -3 -> {
                if (intent.getStatus() == MembershipIntentStatus.PAID) {
                    log.warn("Membership chargeback for paid intent {} — membership already active; manual review may be needed",
                            orderId);
                } else if (intent.getStatus() == MembershipIntentStatus.PENDING) {
                    intent.setStatus(MembershipIntentStatus.FAILED);
                    intentRepository.save(intent);
                }
                log.info("Membership payment CHARGEDBACK for {}", orderId);
            }
            default -> log.warn("Unknown PayHere status code {} for membership order {}", statusCode, orderId);
        }

        if (!paymentId.isEmpty()) {
            try {
                notifyReceiptRepository.save(new PayHereNotifyReceipt(orderId, paymentId, statusCode));
            } catch (DataIntegrityViolationException e) {
                log.info("PayHere duplicate membership notification (race on payment_id): order_id={}, payment_id={}",
                        orderId, paymentId);
            }
        }
    }

    private void verifyCurrency(String payhereCurrency, String orderId) {
        if (payhereCurrency == null || payhereCurrency.isBlank()) {
            log.warn("PayHere membership notify rejected: missing payhere_currency for order_id={}", orderId);
            throw new BadRequestException("Missing payhere_currency");
        }
        if (!"LKR".equalsIgnoreCase(payhereCurrency.trim())) {
            log.warn("PayHere membership notify rejected: currency mismatch for order_id={}", orderId);
            throw new BadRequestException("Currency mismatch");
        }
    }

    private void verifyAmount(String payhereAmount, MembershipPaymentIntent intent, String orderId) {
        if (payhereAmount == null || payhereAmount.isBlank()) {
            log.warn("PayHere membership notify rejected: missing payhere_amount for order_id={}", orderId);
            throw new BadRequestException("Missing payhere_amount");
        }
        BigDecimal notified;
        try {
            notified = new BigDecimal(payhereAmount.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("PayHere membership notify rejected: invalid payhere_amount for order_id={}", orderId);
            throw new BadRequestException("Invalid payhere_amount");
        }
        BigDecimal expected = intent.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (expected.compareTo(notified) != 0) {
            log.warn("PayHere membership notify rejected: amount mismatch for order_id={} (expected {}, got {})",
                    orderId, expected, notified);
            throw new BadRequestException("Payment amount mismatch");
        }
    }

    private boolean isDuplicateNotification(String paymentId, int code, MembershipPaymentIntent intent) {
        if (!paymentId.isEmpty()) {
            return notifyReceiptRepository.existsByPaymentId(paymentId);
        }
        return code == 2 && intent.getStatus() == MembershipIntentStatus.PAID;
    }

    private String generateUniqueOrderNumber() {
        for (int attempt = 0; attempt < 12; attempt++) {
            String candidate = ORDER_PREFIX + System.currentTimeMillis() + "-" + randomSuffix();
            if (!intentRepository.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }
        throw new BadRequestException("Could not generate a unique membership order number");
    }

    private String randomSuffix() {
        String u = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return u.substring(0, Math.min(8, u.length()));
    }
}
