package assignments.insurance;

import java.time.LocalDate;

/** Synthetic command; ownerId is derived from the authenticated server principal, never trusted from client JSON. */
public record PolicyIssueRequest(
        String ownerId,
        String journeyId,
        String quoteVersionId,
        String clientRequestId,
        LocalDate effectiveDate) { }
