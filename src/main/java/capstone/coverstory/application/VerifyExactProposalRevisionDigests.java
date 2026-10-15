package capstone.coverstory.application;

/** CAP-174 learner seam: verify supplied proposal bytes without inventing serialization rules. */
public final class VerifyExactProposalRevisionDigests {
    public enum Status { VERIFIED, DIGEST_MISMATCH, INVALID_MANIFEST, REVIEW_REQUIRED }
    public record Snapshot(String sourceId, String version, String expectedSha256, byte[] body) {
        public Snapshot { body = body == null ? null : body.clone(); }
        @Override public byte[] body() { return body == null ? null : body.clone(); }
    }
    public record Result(Status status, String actualSha256) { }

    public Result verify(Snapshot snapshot, String currentVersion) {
        // LEARNER TODO: validate the manifest, hash the exact supplied bytes (no normalization),
        // compare using the expected digest, and report REVIEW_REQUIRED when this is not the
        // current proposal revision. A matching hash establishes byte identity only.
        throw new UnsupportedOperationException("CAP-174 learner TODO: verify exact revision digest");
    }
}
