package capstone.coverstory.support;

import capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope;
import capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope.EvidenceRow;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CAP177InstructorInfrastructureTest {
    private static List<EvidenceRow> rows() {
        return List.of(new EvidenceRow("r1", "SUBMISSION", "v1", "Offer accepted"),
                new EvidenceRow("r2", "PAYMENT", "p1", "Payment recorded"),
                new EvidenceRow("r3", "DISPATCH_ACK", "d1", "Simulated dispatch acknowledged 🚚"));
    }

    @Test void rowCapKeepsCompletenessAndAbstentionMeaning() {
        var result = CAP177ReferenceOracle.build(rows(), 2, 4096);
        assertEquals(List.of("r1", "r2"), result.envelope().rows().stream().map(EvidenceRow::id).toList());
        assertEquals(2, result.envelope().includedCount()); assertEquals(3, result.envelope().availableCount());
        assertTrue(result.envelope().truncated()); assertTrue(result.safeConclusion().contains("incomplete"));
        assertTrue(result.safeConclusion().contains("omitted events do not establish that dispatch did not happen"));
    }

    @Test void exactByteBudgetIncludesUtf8EnvelopeAndDropsWholeRowBelowBoundary() {
        var all = CAP177ReferenceOracle.build(rows(), 3, 4096);
        int exact = all.utf8().length;
        assertEquals(exact, CAP177ReferenceOracle.build(rows(), 3, exact).utf8().length);
        var smaller = CAP177ReferenceOracle.build(rows(), 3, exact - 1);
        assertTrue(smaller.envelope().truncated()); assertTrue(smaller.envelope().rows().size() < 3);
        assertTrue(smaller.utf8().length <= exact - 1);
        assertEquals(smaller.utf8().length, new String(smaller.utf8(), java.nio.charset.StandardCharsets.UTF_8)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
    }

    @Test void minimumEnvelopeMustFitAndResultCannotAuthorizeMutation() {
        assertThrows(BoundedLifecycleEvidenceEnvelope.TooSmallException.class,
                () -> CAP177ReferenceOracle.build(List.of(), 0, 0));
        assertFalse(java.util.Arrays.stream(BoundedLifecycleEvidenceEnvelope.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().toLowerCase().contains("cancel") || m.getName().toLowerCase().contains("refund")));
    }
}
