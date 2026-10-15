package capstone.coverstory.payments;

import capstone.coverstory.payments.internal.AcceptedRevisionBalance;
import capstone.coverstory.payments.internal.AcceptedRevisionBalance.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/** Independent instructor reference; it is deliberately test-only and never creates an allocation. */
final class CAP116ReferenceBalanceCalculator {
    Reconciliation calculate(AcceptedOffer accepted, List<ConfirmedEffect> observations,
                             List<PendingAllocation> allocations) {
        Set<String> issues = new TreeSet<>();
        if (accepted == null || !accepted.accepted()) {
            issues.add("REVISION_NOT_ACCEPTED");
            return result(accepted, 0, 0, 0, 0, issues, NextAction.REVIEW_DISCREPANCY);
        }
        if (accepted.totalMinor() < 0 || !accepted.currency().matches("^[A-Z]{3}$")
                || !accepted.contentSha256().matches("^[0-9a-f]{64}$")) issues.add("INVALID_ACCEPTED_REVISION_FACTS");

        long captures = 0, refunds = 0, pendingPayments = 0, pendingRefunds = 0;
        Set<UUID> seenEffects = new HashSet<>();
        for (ConfirmedEffect effect : observations) {
            if (!effect.ownerId().equals(accepted.ownerId()) || !effect.applicationId().equals(accepted.applicationId())) continue;
            if (!effect.attachedToAuthoritativeEffect() || effect.effectId() == null) {
                issues.add("ORPHAN_EFFECT_OBSERVATION"); continue;
            }
            if (!effect.currency().equals(accepted.currency())) { issues.add("CURRENCY_MISMATCH"); continue; }
            if (effect.amountMinor() < 0) { issues.add("INVALID_EFFECT_AMOUNT"); continue; }
            if (!seenEffects.add(effect.effectId())) continue;
            try {
                if (effect.kind() == EffectKind.CAPTURE) captures = Math.addExact(captures, effect.amountMinor());
                else refunds = Math.addExact(refunds, effect.amountMinor());
            } catch (ArithmeticException overflow) { issues.add("EFFECT_TOTAL_OVERFLOW"); }
        }
        for (PendingAllocation pending : allocations) {
            if (!pending.ownerId().equals(accepted.ownerId()) || !pending.applicationId().equals(accepted.applicationId())) continue;
            if (pending.amountMinor() < 0 || !pending.currency().equals(accepted.currency())) {
                issues.add("INVALID_PENDING_ALLOCATION"); continue;
            }
            if (pending.status() != AllocationStatus.UNSTARTED && pending.status() != AllocationStatus.IN_FLIGHT
                    && pending.status() != AllocationStatus.UNKNOWN) continue;
            try {
                if (pending.kind() == AllocationKind.ADDITIONAL_PAYMENT) pendingPayments = Math.addExact(pendingPayments, pending.amountMinor());
                else pendingRefunds = Math.addExact(pendingRefunds, pending.amountMinor());
            } catch (ArithmeticException overflow) { issues.add("PENDING_TOTAL_OVERFLOW"); }
        }
        long net = 0, adjustment = 0;
        try { net = Math.subtractExact(captures, refunds); }
        catch (ArithmeticException overflow) { issues.add("NET_TOTAL_OVERFLOW"); }
        if (net < 0) issues.add("CONFIRMED_NET_NEGATIVE");
        try { adjustment = Math.subtractExact(accepted.totalMinor(), net); }
        catch (ArithmeticException overflow) { issues.add("ADJUSTMENT_OVERFLOW"); }

        NextAction action;
        if (!issues.isEmpty()) action = NextAction.REVIEW_DISCREPANCY;
        else if (pendingPayments > 0 || pendingRefunds > 0) action = NextAction.WAIT_OR_RECONCILE;
        else if (adjustment > 0) action = NextAction.CREATE_ADDITIONAL_PAYMENT;
        else if (adjustment < 0) action = NextAction.CREATE_REFUND;
        else action = NextAction.NO_ADJUSTMENT;
        return result(accepted, net, pendingPayments, pendingRefunds, adjustment, issues, action);
    }

    private static Reconciliation result(AcceptedOffer accepted, long net, long pendingPayments, long pendingRefunds,
                                         long adjustment, Set<String> issues, NextAction action) {
        return new Reconciliation(accepted == null ? null : accepted.offerId(), accepted == null ? null : accepted.revision(),
                accepted == null ? null : accepted.contentSha256(), net, pendingPayments, pendingRefunds, adjustment,
                accepted == null ? "INR" : accepted.currency(), issues, action);
    }
}
