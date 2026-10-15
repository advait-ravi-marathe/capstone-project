package coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.application.AdaptOnboardingAssistance.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Independent, test-only oracle; does not repair or invoke the learner seam. */
class CAP91InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-91/synthetic-v1.json");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void syntheticFixtureDefinesAllFourCasesAndNoExternalCall() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("actual_model_call").asBoolean());
        assertEquals(4, root.path("cases").size());
        assertEquals("400001", root.path("manual_draft").path("pin").asText());
        assertTrue(root.path("permitted_evidence").get(0).path("sourceId").isTextual());
    }

    @Test void independentReferenceRetainsExactSuccessMetadataAndNeverWrites() {
        Citation citation = new Citation("guide-health-v1", "1", 28, 41);
        Usage usage = new Usage(120, 20);
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger writes = new AtomicInteger();
        Result result = reference((q, e) -> {
            calls.incrementAndGet();
            return new ModelDraft("The proposal uses the six digit PIN.", null, List.of(citation), usage);
        }, QUESTION, EVIDENCE, MANUAL);
        assertEquals(1, calls.get());
        assertEquals(0, writes.get());
        assertEquals(List.of(citation), result.citations());
        assertEquals(usage, result.usage());
        assertEquals(MANUAL, result.manualDraft());
        assertEquals(Outcome.SUGGESTED, result.outcome());
    }

    @Test void timeoutAndMissingCitationsFailClosedToManualDraft() {
        AtomicInteger writes = new AtomicInteger();
        Result timeout = reference((q, e) -> { throw new AssistTimeout("offline timeout"); }, QUESTION, EVIDENCE, MANUAL);
        Result malformed = reference((q, e) -> new ModelDraft("uncited", null, null, new Usage(1, 1)), QUESTION, EVIDENCE, MANUAL);
        Result unsupportedCitation = reference((q, e) -> new ModelDraft("wrong source", null,
                List.of(new Citation("unpermitted-source", "1", 0, 5)), new Usage(1, 1)), QUESTION, EVIDENCE, MANUAL);
        assertEquals(Outcome.TIMEOUT, timeout.outcome());
        assertEquals(MANUAL, timeout.manualDraft());
        assertEquals(Outcome.MALFORMED, malformed.outcome());
        assertNull(malformed.answer());
        assertEquals(Outcome.MALFORMED, unsupportedCitation.outcome());
        assertNull(unsupportedCitation.answer());
        assertEquals(0, writes.get());
    }

    @Test void differingPinIsOnlyAnUnconfirmedSuggestionAndLeavesDraftAlone() {
        Result result = reference((q, e) -> new ModelDraft("Check the suggested PIN.", "560001",
                List.of(new Citation("guide-health-v1", "1", 28, 41)), new Usage(120, 20)),
                QUESTION, EVIDENCE, MANUAL);
        assertEquals("400001", result.manualDraft().pin());
        assertEquals(new Suggestion("560001", false), result.suggestion());
        assertEquals(Outcome.SUGGESTED, result.outcome());
    }

    private static Result reference(OnboardingAssistPort port, AuthorizedQuestion question,
                                    List<EvidenceChunk> evidence, ManualDraft draft) {
        final ModelDraft response;
        try { response = port.suggest(question, List.copyOf(evidence)); }
        catch (AssistTimeout ex) { return new Result(Outcome.TIMEOUT, null, List.of(), null, null, draft); }
        if (response == null || response.answer() == null || response.answer().isBlank()
                || response.citations() == null || response.citations().isEmpty() || response.usage() == null)
            return new Result(Outcome.MALFORMED, null, List.of(), null, null, draft);
        for (Citation citation : response.citations()) {
            boolean backed = evidence.stream().anyMatch(chunk -> chunk.sourceId().equals(citation.sourceId())
                    && chunk.version().equals(citation.version()) && citation.start() >= 0
                    && citation.end() > citation.start() && citation.end() <= chunk.text().length());
            if (!backed) return new Result(Outcome.MALFORMED, null, List.of(), null, null, draft);
        }
        Suggestion suggestion = response.suggestedPin() == null ? null : new Suggestion(response.suggestedPin(), false);
        return new Result(Outcome.SUGGESTED, response.answer(), response.citations(), response.usage(), suggestion, draft);
    }

    private static final AuthorizedQuestion QUESTION = new AuthorizedQuestion("owner-A", "Which PIN is required?");
    private static final List<EvidenceChunk> EVIDENCE = List.of(new EvidenceChunk("guide-health-v1", "1",
            "For this example, enter the six digit PIN on the proposal."));
    private static final ManualDraft MANUAL = new ManualDraft("400001", "synthetic draft stays unchanged");
}
