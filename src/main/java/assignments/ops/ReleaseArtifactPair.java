package assignments.ops;

import java.util.Objects;

/** Immutable identity of the two artifacts exercised by one local compatibility run. */
public record ReleaseArtifactPair(ArtifactIdentity baseline, ArtifactIdentity candidate,
                                  String fixtureIdentity, String schemaFingerprint,
                                  boolean legacyMatrixPassed, boolean reportModePassed,
                                  boolean retainedStateVerified, boolean cleanupPassed) {
    public ReleaseArtifactPair {
        Objects.requireNonNull(baseline);
        Objects.requireNonNull(candidate);
        Objects.requireNonNull(fixtureIdentity);
        Objects.requireNonNull(schemaFingerprint);
    }

    public record ArtifactIdentity(String sha256, String sourceRevision, String capabilityId,
                                   String buildRecipe, boolean builtWithTemporaryWitness) {
        public ArtifactIdentity {
            Objects.requireNonNull(sha256);
            Objects.requireNonNull(sourceRevision);
            Objects.requireNonNull(capabilityId);
            Objects.requireNonNull(buildRecipe);
        }
    }
}
