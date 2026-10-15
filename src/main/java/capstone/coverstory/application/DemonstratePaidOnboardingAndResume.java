package capstone.coverstory.application;

import java.util.UUID;

/** Day 48 learner seam. All supplied payment, draft, and policy values are synthetic. */
public final class DemonstratePaidOnboardingAndResume {
    public record Request(UUID authenticatedOwner, UUID journeyId, UUID applicationId, int revision,
                          String syntheticPaymentEventId, String idempotencyKey) { }
    public record Resume(String status, UUID journeyId, UUID applicationId, int revision,
                         String stepCode, boolean protectedPayloadAvailable) { }

    public Resume resume(Request request) {
        // LEARNER TODO: resolve the authoritative owned journey and immutable revision,
        // validate confirmed synthetic payment, authorize before decrypt, restore only
        // the allowlisted step, and make duplicate callbacks idempotent. Never infer
        // payment, submission, or issuance from browser state or analytics.
        throw new UnsupportedOperationException("CAP-87 learner TODO: demonstrate paid onboarding resume");
    }
}
