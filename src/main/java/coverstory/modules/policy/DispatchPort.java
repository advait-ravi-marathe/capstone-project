package coverstory.modules.policy;

import java.util.UUID;

/** Simulated insurer dispatch boundary; payment alone never activates cover. */
public interface DispatchPort {
    DispatchState request(UUID authenticatedOwnerId, UUID applicationId, long revision, UUID paymentId);
}
