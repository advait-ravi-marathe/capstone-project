package coverstory.modules.payments.api;

import capstone.coverstory.payments.internal.AcceptedRevisionBalance.PendingAllocation;
import java.util.List;
import java.util.UUID;

/** Payments-owned pending adjustment reservations, read only. */
public interface PendingAdjustmentFactsPort {
    List<PendingAllocation> findPendingAllocations(UUID applicationId);
}
