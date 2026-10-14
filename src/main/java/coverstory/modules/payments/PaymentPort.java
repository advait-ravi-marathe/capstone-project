package coverstory.modules.payments;

import java.util.UUID;

/** Simulation only: this interface never connects to a real payment provider. */
public interface PaymentPort {
    SimulatedPayment begin(UUID authenticatedOwnerId, UUID applicationId, String idempotencyKey, Money amount);
    SimulatedPayment reconcile(UUID authenticatedOwnerId, UUID paymentId);
    void cancel(UUID authenticatedOwnerId, UUID paymentId);
}
