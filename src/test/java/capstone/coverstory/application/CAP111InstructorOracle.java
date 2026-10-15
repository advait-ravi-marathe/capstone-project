package capstone.coverstory.application;

import java.util.Map;
import java.util.UUID;

/** Independent test oracle for submission eligibility and explicit pending state. */
final class CAP111InstructorOracle {
    record Facts(long currentRevision, long reviewedRevision, UUID currentQuote, UUID reviewedQuote,
                 String currentConfigHash, String reviewedConfigHash, String currentTermsHash,
                 String reviewedTermsHash, boolean requiredComplete, boolean disclosuresComplete,
                 boolean extractionConfirmed, boolean paymentReconciled, boolean refundReconciled) { }
    record Decision(boolean eligible, int status, String code, boolean dispatchCreated) { }

    static Decision evaluate(Facts f, boolean explicitConsent) {
        if (!explicitConsent) return new Decision(false, 400, "EXPLICIT_CONSENT_REQUIRED", false);
        if (f.currentRevision() != f.reviewedRevision() || !f.currentQuote().equals(f.reviewedQuote())
                || !f.currentConfigHash().equals(f.reviewedConfigHash())
                || !f.currentTermsHash().equals(f.reviewedTermsHash()))
            return new Decision(false, 409, "REVIEW_STALE", false);
        if (!f.requiredComplete() || !f.disclosuresComplete() || !f.extractionConfirmed()
                || !f.paymentReconciled() || !f.refundReconciled())
            return new Decision(false, 409, "REVIEW_BLOCKED", false);
        return new Decision(true, 202, "SUBMISSION_PENDING", false);
    }

    static Map<String, String> capacityCaveats() {
        return Map.of("measurement", "not_benchmarked", "load_shape", "assumption_only",
                "cache_origin_rate", "depends_on_static_hit_ratio", "capacity_claim", "not_proven");
    }
}
