package coverstory.modules.payments.api;

import java.util.UUID;

/** Payments-owned read-only quote/payment facts; explanation validation cannot invoke payment. */
public interface OfferPaymentFactsPort {
    PaymentFact findForOffer(UUID offerId);
    record PaymentFact(String status, long paidAmountMinor, boolean dispatchConfirmed) { }
}
