package coverstory.modules.payments.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Read-only Payments-owned journal query. Owner comes from the authenticated request principal. */
@FunctionalInterface
public interface PaymentAuditWindowReadPort {
    List<UUID> selectPaymentAudit(UUID ownerId, Instant startInclusive, Instant endExclusive);
}
