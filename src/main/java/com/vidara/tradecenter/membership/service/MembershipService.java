package com.vidara.tradecenter.membership.service;

import com.vidara.tradecenter.cart.dto.response.CartItemResponse;
import com.vidara.tradecenter.cart.dto.response.CartResponse;
import com.vidara.tradecenter.common.dto.PagedResponse;
import com.vidara.tradecenter.membership.dto.AdminMembershipRowResponse;
import com.vidara.tradecenter.membership.dto.MembershipMeResponse;
import com.vidara.tradecenter.membership.dto.MembershipPlanResponse;
import com.vidara.tradecenter.membership.model.UserMembership;
import com.vidara.tradecenter.membership.model.enums.MembershipBillingPeriod;
import com.vidara.tradecenter.membership.model.enums.MembershipPlan;
import com.vidara.tradecenter.membership.model.enums.MembershipRecordStatus;
import com.vidara.tradecenter.membership.repository.UserMembershipRepository;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MembershipService {

    private final UserMembershipRepository userMembershipRepository;
    private final UserRepository userRepository;

    public MembershipService(UserMembershipRepository userMembershipRepository,
            UserRepository userRepository) {
        this.userMembershipRepository = userMembershipRepository;
        this.userRepository = userRepository;
    }

    public Optional<MembershipPlan> getActivePlan(Long userId) {
        return userMembershipRepository.findByUserIdAndStatus(userId, MembershipRecordStatus.ACTIVE)
                .map(UserMembership::getPlan);
    }

    public MembershipMeResponse getMe(Long userId) {
        MembershipMeResponse dto = new MembershipMeResponse();
        Optional<UserMembership> opt = userMembershipRepository.findByUserIdAndStatus(userId,
                MembershipRecordStatus.ACTIVE);
        if (opt.isEmpty()) {
            dto.setActive(false);
            dto.setProductDiscountPercent(0);
            return dto;
        }
        UserMembership m = opt.get();
        dto.setActive(true);
        dto.setPlan(m.getPlan());
        dto.setBillingPeriod(m.getBillingPeriod());
        dto.setProductDiscountPercent(MembershipPricingCalculator.membershipProductDiscountPercent(m.getPlan()));
        return dto;
    }

    public List<MembershipPlanResponse> listPlans() {
        List<MembershipPlanResponse> list = new ArrayList<>();
        list.add(planDto(MembershipPlan.STARTER, "Starter", false));
        list.add(planDto(MembershipPlan.PROFESSIONAL, "Professional", true));
        list.add(planDto(MembershipPlan.ENTERPRISE, "Enterprise", false));
        return list;
    }

    private MembershipPlanResponse planDto(MembershipPlan plan, String title, boolean popular) {
        MembershipPlanResponse r = new MembershipPlanResponse();
        r.setPlan(plan);
        r.setTitle(title);
        r.setMonthlyPrice(MembershipPlanCatalog.monthlyFee(plan));
        r.setYearlyPrice(MembershipPlanCatalog.yearlyFee(plan));
        r.setProductDiscountPercent(MembershipPricingCalculator.membershipProductDiscountPercent(plan));
        r.setSubtitle(r.getProductDiscountPercent() + "% off everything");
        r.setMostPopular(popular);
        r.setFeatures(planFeatures(plan));
        return r;
    }

    private List<String> planFeatures(MembershipPlan plan) {
        return switch (plan) {
            case STARTER -> List.of(
                    "5% off all products",
                    "Free standard shipping on eligible orders",
                    "Early access to sales",
                    "Monthly deals newsletter");
            case PROFESSIONAL -> List.of(
                    "12% off all products",
                    "Free express shipping on eligible orders",
                    "Priority customer support",
                    "Exclusive member-only products",
                    "5% extra on bulk orders (10+ units)",
                    "Early access to new arrivals");
            case ENTERPRISE -> List.of(
                    "20% off all products",
                    "Free same-day shipping where available",
                    "Dedicated account manager",
                    "Custom bulk pricing",
                    "8% extra on bulk orders (10+ units)",
                    "Invoice & NET 30 payment terms",
                    "API access",
                    "Volume forecasting tools");
        };
    }

    /**
     * Activates or replaces the user's membership after a successful PayHere payment (upgrade / downgrade / new).
     */
    @Transactional(readOnly = false)
    public void activatePaidSubscription(Long userId, MembershipPlan plan, MembershipBillingPeriod billingPeriod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userMembershipRepository.findByUserIdAndStatus(userId, MembershipRecordStatus.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(MembershipRecordStatus.CANCELLED);
                    existing.setCancelledAt(LocalDateTime.now());
                    userMembershipRepository.save(existing);
                });

        UserMembership m = new UserMembership();
        m.setUser(user);
        m.setPlan(plan);
        m.setBillingPeriod(billingPeriod);
        m.setStatus(MembershipRecordStatus.ACTIVE);
        userMembershipRepository.save(m);
    }

    @Transactional(readOnly = false)
    public MembershipMeResponse cancel(Long userId) {
        userMembershipRepository.findByUserIdAndStatus(userId, MembershipRecordStatus.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(MembershipRecordStatus.CANCELLED);
                    existing.setCancelledAt(LocalDateTime.now());
                    userMembershipRepository.save(existing);
                });
        return getMe(userId);
    }

    public void applyPricingToCartResponse(CartResponse response, Long userId) {
        MembershipPlan plan = getActivePlan(userId).orElse(null);
        response.setMembershipActive(plan != null);
        response.setMembershipPlan(plan != null ? plan.name() : null);
        response.setMembershipProductDiscountPercent(
                plan != null ? MembershipPricingCalculator.membershipProductDiscountPercent(plan) : 0);

        BigDecimal sum = BigDecimal.ZERO;
        for (CartItemResponse item : response.getItems()) {
            BigDecimal retail = item.getPrice();
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            int bulk = MembershipPricingCalculator.bulkDiscountPercent(qty);
            int mem = MembershipPricingCalculator.membershipProductDiscountPercent(plan);
            BigDecimal lineDisc = MembershipPricingCalculator.combinedDiscountPercent(bulk, mem);
            if (lineDisc.compareTo(MembershipPricingCalculator.MAX_COMBINED_DISCOUNT_PERCENT) > 0) {
                lineDisc = MembershipPricingCalculator.MAX_COMBINED_DISCOUNT_PERCENT;
            }
            BigDecimal effUnit = MembershipPricingCalculator.effectiveUnitPrice(retail, qty, plan);
            BigDecimal effSub = effUnit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

            item.setRetailUnitPrice(retail);
            item.setBulkDiscountPercent(bulk);
            item.setMembershipDiscountPercent(mem);
            item.setLineDiscountPercent(lineDisc);
            item.setEffectiveUnitPrice(effUnit);
            item.setEffectiveSubtotal(effSub);
            sum = sum.add(effSub);
        }
        response.setTotalAmount(sum);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PagedResponse<AdminMembershipRowResponse> adminList(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<UserMembership> p = userMembershipRepository.findByStatusOrderByCreatedAtDesc(
                MembershipRecordStatus.ACTIVE, pageable);
        List<AdminMembershipRowResponse> rows = p.getContent().stream().map(m -> {
            AdminMembershipRowResponse r = new AdminMembershipRowResponse();
            r.setId(m.getId());
            r.setUserId(m.getUser().getId());
            r.setCustomerEmail(m.getUser().getEmail());
            r.setPlan(m.getPlan());
            r.setBillingPeriod(m.getBillingPeriod());
            r.setStatus(m.getStatus());
            r.setCreatedAt(m.getCreatedAt());
            r.setCancelledAt(m.getCancelledAt());
            return r;
        }).toList();
        return PagedResponse.of(rows, p);
    }
}
