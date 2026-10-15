package coverstory.modules.payments.api;

import coverstory.modules.payments.api.CAP80Seam.OwnerPaymentItem;
import coverstory.modules.payments.api.CAP80Seam.PaymentCheckpoint;

import java.util.Optional;
import java.util.UUID;

/** Public Payments read boundary for immutable synthetic confirmations and checkpoints. */
public interface MockConfirmationFeedPort {
    Optional<OwnerPaymentItem> findOwned(UUID ownerId, String eventId);
    Optional<PaymentCheckpoint> currentCheckpoint(UUID ownerId);
    CAP80Seam.PaymentEffectPort effectPort();
}
