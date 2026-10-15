package coverstory.modules.payments.api;

import java.util.UUID;

/** Payments-owned, read-only payment state; alert rendering must never imply dispatched or active cover. */
public interface ReviewPaymentFactsPort {
    PaymentFact findForApplication(UUID applicationId);
    record PaymentFact(String status, boolean dispatchConfirmed) { }
}
