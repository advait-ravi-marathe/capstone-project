package assignments.insurance;

import java.time.LocalDate;

public record PolicyCancellationRequest(
        long expectedVersion,
        String clientRequestId,
        LocalDate effectiveDate,
        String reasonCode) { }
