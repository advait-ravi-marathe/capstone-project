package coverstory.application;

import coverstory.application.AdaptOnboardingAssistance.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP91AcceptanceTest {
    private static final AuthorizedQuestion QUESTION = new AuthorizedQuestion("owner-A", "Which PIN is required?");
    private static final List<EvidenceChunk> EVIDENCE = List.of(new EvidenceChunk("guide-health-v1", "1",
            "For this example, enter the six digit PIN on the proposal."));
    private static final ManualDraft MANUAL = new ManualDraft("400001", "synthetic draft stays unchanged");

    @Test void successfulResponseRetainsExactCitationAndUsageFromOneCall() {
        Citation citation = new Citation("guide-health-v1", "1", 28, 41);
        Usage usage = new Usage(120, 20);
        AtomicInteger calls = new AtomicInteger();
        AdaptOnboardingAssistance seam = new AdaptOnboardingAssistance((q, evidence) -> {
            calls.incrementAndGet();
            assertEquals(QUESTION, q);
            assertEquals(EVIDENCE, evidence);
            return new ModelDraft("The proposal uses the six digit PIN.", null, List.of(citation), usage);
        });
        Result result = seam.suggest(QUESTION, EVIDENCE, MANUAL);
        assertEquals(1, calls.get());
        assertEquals(Outcome.SUGGESTED, result.outcome());
        assertEquals(List.of(citation), result.citations());
        assertEquals(usage, result.usage());
        assertEquals(MANUAL, result.manualDraft());
    }

    @Test void timeoutLeavesManualEntryAvailableAndWritesNothing() {
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger writes = new AtomicInteger();
        AdaptOnboardingAssistance seam = new AdaptOnboardingAssistance((q, e) -> {
            calls.incrementAndGet(); throw new AssistTimeout("synthetic timeout");
        });
        Result result = seam.suggest(QUESTION, EVIDENCE, MANUAL);
        assertEquals(1, calls.get());
        assertEquals(0, writes.get());
        assertEquals(Outcome.TIMEOUT, result.outcome());
        assertEquals(MANUAL, result.manualDraft());
    }

    @Test void absentCitationArrayIsMalformedAndReturnsNoAnswerOrWrite() {
        AtomicInteger writes = new AtomicInteger();
        AdaptOnboardingAssistance seam = new AdaptOnboardingAssistance((q, e) ->
                new ModelDraft("Enter the PIN.", null, null, new Usage(9, 3)));
        Result result = seam.suggest(QUESTION, EVIDENCE, MANUAL);
        assertEquals(Outcome.MALFORMED, result.outcome());
        assertNull(result.answer());
        assertEquals(0, writes.get());
        assertEquals(MANUAL, result.manualDraft());
    }

    @Test void differingPinRemainsAnUnconfirmedSuggestionWithoutRequoteOrOverwrite() {
        AtomicInteger writes = new AtomicInteger();
        AtomicInteger requotes = new AtomicInteger();
        AdaptOnboardingAssistance seam = new AdaptOnboardingAssistance((q, e) -> new ModelDraft(
                "A different synthetic PIN may apply.", "560001",
                List.of(new Citation("guide-health-v1", "1", 28, 41)), new Usage(120, 20)));
        Result result = seam.suggest(QUESTION, EVIDENCE, MANUAL);
        assertEquals(Outcome.SUGGESTED, result.outcome());
        assertEquals("400001", result.manualDraft().pin());
        assertEquals(new Suggestion("560001", false), result.suggestion());
        assertEquals(0, requotes.get());
        assertEquals(0, writes.get());
    }
}
