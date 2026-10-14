package coverstory.modules.payments;

import java.util.UUID;

public record SimulatedPayment(UUID paymentId, State state, Money amount) {
    public enum State { RECORDED, DISPATCH_PENDING, CANCELLED, RECONCILIATION_REQUIRED }
}
