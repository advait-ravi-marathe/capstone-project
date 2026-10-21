package assignments.ops;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class ReleasePairAcceptanceTest {
    private final ReleaseArtifactPairPolicy policy = new ReleaseArtifactPairPolicy();
    @Test void day141RejectsSameBinaryAndLabelOnlyDifferences() {
        var a = new ReleaseArtifactPair.ArtifactIdentity("a".repeat(64), "source-a", "legacy-inspect", "mvn-clean-package", true);
        var sameJarRenamed = new ReleaseArtifactPair.ArtifactIdentity("a".repeat(64), "source-a", "legacy-inspect", "mvn-clean-package", true);
        assertThrows(IllegalArgumentException.class, () -> policy.validate(new ReleaseArtifactPair(a, sameJarRenamed, "fixture-1", "V001-V005", true, true, true, true)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(new ReleaseArtifactPair(a,
                new ReleaseArtifactPair.ArtifactIdentity("b".repeat(64), "source-b", "legacy-inspect", "renamed-output", true),
                "fixture-1", "V001-V005", true, true, true, true)));
        var candidate=new ReleaseArtifactPair.ArtifactIdentity("b".repeat(64),"source-b","report-v1","isolated-clean-build",true);
        var valid=new ReleaseArtifactPair(a,candidate,"fixture-1","schema-fingerprint",true,true,true,true);
        assertSame(valid,policy.validate(valid));
        assertThrows(IllegalArgumentException.class,()->policy.validate(new ReleaseArtifactPair(
                new ReleaseArtifactPair.ArtifactIdentity("short","source-a","legacy-inspect","mvn-clean-package",true),candidate,
                "fixture-1","schema-fingerprint",true,true,true,true)));
    }
}
