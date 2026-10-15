package capstone.coverstory.support;

import capstone.coverstory.support.internal.SourceLineageReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class SourceLineageReaderTest {
    private static final SourceLineageReader.Citation A = new SourceLineageReader.Citation(
            "ai1", "owner-A", "policy-A", "v2", "m2", "pA", 23, 39);

    @Test void completeAuthorizedChainReturnsExactVersionBatchEvidenceAndZeroWrites() {
        var result = SourceLineageReader.resolve(new SourceLineageReader.Principal("owner-A", "owner-A"),
                new SourceLineageReader.Request("ai1"), port(A, "v2", "m2", "pA"));
        assertEquals("FOUND", result.status());
        assertEquals("v2", result.lineage().versionId());
        assertEquals("m2", result.lineage().batchId());
        assertEquals("pA", result.lineage().evidenceId());
        assertEquals(23, result.lineage().startByte());
        assertEquals(39, result.lineage().endByte());
    }

    @Test void missingV2WithV1PresentFailsWithoutFallback() {
        var result = SourceLineageReader.resolve(new SourceLineageReader.Principal("owner-A", "owner-A"),
                new SourceLineageReader.Request("ai1"), port(A, null, "m1", "pA"));
        assertEquals("LINEAGE_BROKEN", result.status());
        assertEquals("EXACT_VERSION_MISSING", result.code());
        assertNull(result.lineage());
    }

    @Test void ownerACannotLearnAnythingAboutBOnlyCitation() {
        var b = new SourceLineageReader.Citation("bi1", "owner-B", "policy-B", "v2", "m2B", "pB", 23, 39);
        var result = SourceLineageReader.resolve(new SourceLineageReader.Principal("owner-A", "owner-A"),
                new SourceLineageReader.Request("bi1"), port(b, "v2", "m2B", "pB"));
        assertEquals("NOT_FOUND", result.status());
        assertEquals("Citation not found.", result.safeMessage());
        assertNull(result.lineage());
    }

    private static SourceLineageReader.ReadPort port(SourceLineageReader.Citation c, String foundVersion,
                                                      String batch, String evidence) {
        return new SourceLineageReader.ReadPort() {
            public SourceLineageReader.Citation citation(String id) { return id.equals(c.id()) ? c : null; }
            public String exactVersionOwner(String id) { return id.equals(foundVersion) ? c.ownerId() : null; }
            public String exactBatchOwner(String id) { return id.equals(batch) ? c.ownerId() : null; }
            public String exactEvidenceOwner(String id) { return id.equals(evidence) ? c.ownerId() : null; }
        };
    }
}
