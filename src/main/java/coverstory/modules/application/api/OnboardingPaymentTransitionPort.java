package coverstory.modules.application.api;

import coverstory.modules.payments.api.CAP80Seam.OwnerPaymentItem;

import java.util.UUID;

/** Application-owned effect called inside the Payments module's shared transaction. */
public interface OnboardingPaymentTransitionPort {
    void unlock(UUID authenticatedOwner, OwnerPaymentItem confirmedItem);
}
