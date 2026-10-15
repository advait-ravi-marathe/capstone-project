package capstone.coverstory.support;

import capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope;
import capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope.EvidenceRow;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class BoundedLifecycleEvidenceEnvelopeTest {
    private static List<EvidenceRow> rows() {
        return List.of(new EvidenceRow("r1", "SUBMISSION", "v1", "Offer accepted"),
                new EvidenceRow("r2", "PAYMENT", "p1", "Payment recorded"),
                new EvidenceRow("r3", "DISPATCH_ACK", "d1", "Simulated dispatch acknowledged 🚚"));
    }

    @Test void rowLimitCarriesTruncationAndNeverClaimsDispatchDidNotHappen() {
        var result = new BoundedLifecycleEvidenceEnvelope().build(rows(), 2, 4096);
        assertEquals(List.of("r1", "r2"), result.rows().stream().map(EvidenceRow::id).toList());
        assertEquals(2, result.includedCount()); assertEquals(3, result.availableCount()); assertTrue(result.truncated());
        assertTrue(result.truncated(), "omitted dispatch acknowledgement must not be interpreted as absence");
    }

    @Test void exactUtf8BudgetFitsWholeEnvelopeAndOneByteLessNeverSplitsUnicodeOrJson() {
        var full = CAP177ReferenceOracle.build(rows(), 3, 4096);
        int exact = full.utf8().length;
        var equal = new BoundedLifecycleEvidenceEnvelope().build(rows(), 3, exact);
        assertArrayEquals(full.utf8(), CAP177ReferenceOracle.serialize(equal));
        var smaller = new BoundedLifecycleEvidenceEnvelope().build(rows(), 3, exact - 1);
        assertTrue(smaller.truncated()); assertTrue(smaller.rows().size() < 3);
        assertTrue(CAP177ReferenceOracle.serialize(smaller).length <= exact - 1);
    }

    @Test void emptyEnvelopeUnderMinimumByteBudgetIsTooSmall() {
        var error = assertThrows(BoundedLifecycleEvidenceEnvelope.TooSmallException.class,
                () -> new BoundedLifecycleEvidenceEnvelope().build(List.of(), 0, 0));
        assertEquals("TOO_SMALL", error.code());
    }

    @Test void fixtureIsSyntheticAndBindsTheRowAndByteBoundaries() throws Exception {
        var tree = new ObjectMapper().readTree(Files.readString(Path.of("docs/coverstory/fixtures/cap-177.json")));
        assertEquals("SYNTHETIC_ONLY_NO_NETWORK", tree.path("classification").asText());
        assertEquals(3, tree.path("rows").size()); assertEquals(2, tree.path("maxRows").asInt());
        assertTrue(tree.path("rows").get(2).path("summary").asText().contains("🚚"));
        assertTrue(tree.toString().contains("truncated"));
    }
}
