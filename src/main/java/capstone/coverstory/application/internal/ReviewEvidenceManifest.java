package capstone.coverstory.application.internal;

import java.time.Instant;
import java.util.List;

/** Read-only learner seam. Verified files prove integrity and scoped facts, never insurer approval. */
public final class ReviewEvidenceManifest {
    public enum Status { VERIFIED_REVIEW_REQUIRED, INVALID }
    public enum NextStep { RECONCILE_BALANCE, NONE }
    public record Artifact(String artifactId, String artifactType, String path, String sha256, String revision) {}
    public record Manifest(String ownerId, String applicationId, String acceptedRevision,
                           String acceptedOfferContentSha256, Instant verifiedAt, Instant offerExpiresAt,
                           List<String> termCodes, List<Artifact> artifacts, String narrative) {}
    public record Verdict(Status status, NextStep nextStep, List<String> issues) {}

    public Verdict verify(Manifest manifest) {
        throw new UnsupportedOperationException("Learner TODO: verify the complete revision-bound evidence manifest");
    }
}
