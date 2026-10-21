package assignments.insurance;

import java.time.LocalDate;

/** Instructor-owned fixture supplied by the authenticated API and quote lookup adapters. */
public record PolicyIssueFacts(String authenticatedOwnerId, String quoteOwnerId, String journeyId,
                               String quoteVersionId, boolean selected, boolean eligible,
                               LocalDate quoteExpiresOn, boolean quoteFound) { }
