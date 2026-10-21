package assignments.insurance;

import java.time.LocalDate;

/** Synthetic client command; authenticated owner identity is supplied separately by the server. */
public record PolicyIssueRequest(
        String journeyId,
        String quoteVersionId,
        String clientRequestId,
        LocalDate effectiveDate) { }
