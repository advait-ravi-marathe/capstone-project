package assignments.insurance;

import java.time.LocalDate;

/** Instructor-owned idempotency fixture for one synthetic cancellation command. */
public record PolicyCancellationReceipt(String ownerId, String clientRequestId, LocalDate effectiveDate,
                                        String reasonCode, PolicySnapshot originalSnapshot) { }
