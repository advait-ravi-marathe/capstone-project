package assignments.ops;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class BinaryFallbackAcceptanceTest {
    private final BinaryFallbackPolicy policy = new BinaryFallbackPolicy();

    @Test void day150RequiresBoundRuntimeEvidenceBeforeAResumes() {
        var a = new ReleaseArtifactPair.ArtifactIdentity("a".repeat(64), "rev-a", "legacy-v1", "isolated-build-A", true);
        var b = new ReleaseArtifactPair.ArtifactIdentity("b".repeat(64), "rev-b", "inspection-v1", "isolated-build-B", true);
        var passed = new ReleaseArtifactPair(a,b,"fixture-123","schema-v001-v005",true,true,true,true);
        assertEquals(BinaryFallbackPolicy.FallbackDecision.ALLOW_TESTED_PAIR,
                policy.decide(passed,a.sha256(),b.sha256(),"fixture-123","schema-v001-v005"));
        assertEquals(BinaryFallbackPolicy.FallbackDecision.BLOCKED,
                policy.decide(passed,a.sha256(),"c".repeat(64),"fixture-123","schema-v001-v005"));
        assertEquals(BinaryFallbackPolicy.FallbackDecision.BLOCKED,
                policy.decide(new ReleaseArtifactPair(a,b,"fixture-123","schema-v001-v005",false,true,true,true),
                        a.sha256(),b.sha256(),"fixture-123","schema-v001-v005"));
        assertEquals(BinaryFallbackPolicy.FallbackDecision.BLOCKED,
                policy.decide(passed,a.sha256(),b.sha256(),"other-fixture","schema-v001-v005"));
        assertEquals(BinaryFallbackPolicy.FallbackDecision.BLOCKED,
                policy.decide(passed,a.sha256(),b.sha256(),"fixture-123","changed-current-schema"),
                "a current schema fingerprint different from the tested fingerprint must block fallback");
    }

    @Test void day151BlocksMissingOrMisleadingCandidateEvidence() {
        var a = new ReleaseArtifactPair.ArtifactIdentity("a".repeat(64), "rev-a", "legacy-v1", "isolated-build-A", true);
        var b = new ReleaseArtifactPair.ArtifactIdentity("b".repeat(64), "rev-b", "inspection-v1", "isolated-build-B", true);
        var misleading = new ReleaseArtifactPair(a,b,"fixture-123","schema-v001-v005",true,false,true,true);
        assertNotEquals(BinaryFallbackPolicy.FallbackDecision.ALLOW_TESTED_PAIR,
                policy.decide(misleading,a.sha256(),b.sha256(),"fixture-123","schema-v001-v005"));
    }
}
