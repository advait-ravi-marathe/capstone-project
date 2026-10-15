package coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Learner-owned report contract. Fails at the explicit TODO until implemented. */
@Tag("learnerAcceptance")
class CAP97AcceptanceTest {
    private final ObjectMapper json = new ObjectMapper();
    private final CompareGroundedAssistanceWithGuessing seam = new CompareGroundedAssistanceWithGuessing();

    private JsonNode fixture() throws Exception {
        return json.readTree(Files.readString(Path.of("fixtures/coverstory/CAP-97/synthetic-v1.json")));
    }

    private List<CompareGroundedAssistanceWithGuessing.Question> questions(JsonNode root) {
        List<CompareGroundedAssistanceWithGuessing.Question> result = new ArrayList<>();
        for (JsonNode row : root.path("questions")) {
            Set<String> permitted = new HashSet<>(); row.path("permitted_source_ids").forEach(v -> permitted.add(v.asText()));
            result.add(new CompareGroundedAssistanceWithGuessing.Question(row.path("id").asText(),
                    row.path("owner_id").asText(), row.path("answerable").asBoolean(),
                    row.path("supported_answer").isNull() ? null : row.path("supported_answer").asText(), permitted));
        }
        return result;
    }

    private List<CompareGroundedAssistanceWithGuessing.Source> sources(JsonNode root) {
        List<CompareGroundedAssistanceWithGuessing.Source> result = new ArrayList<>();
        for (JsonNode row : root.path("sources")) {
            Set<String> supports = new HashSet<>(); row.path("supports_question_ids").forEach(v -> supports.add(v.asText()));
            result.add(new CompareGroundedAssistanceWithGuessing.Source(row.path("id").asText(),
                    row.path("owner_id").asText(), row.path("kind").asText(), supports));
        }
        return result;
    }

    private CompareGroundedAssistanceWithGuessing.ModeOutput mode(String name, JsonNode root) {
        List<CompareGroundedAssistanceWithGuessing.Explanation> rows = new ArrayList<>();
        root.path(name).forEach(row -> {
            List<String> citations = new ArrayList<>(); row.path("citations").forEach(v -> citations.add(v.asText()));
            rows.add(new CompareGroundedAssistanceWithGuessing.Explanation(row.path("question_id").asText(),
                    row.path("answer").isNull() ? null : row.path("answer").asText(), citations));
        });
        return new CompareGroundedAssistanceWithGuessing.ModeOutput(name, rows);
    }

    @Test void modelOnlyKeepsItsOneSupportedAnswerAndBothUnsafeGuesses() throws Exception {
        JsonNode f = fixture();
        var report = seam.scoreModes(questions(f), sources(f), mode("model_only", f), mode("source_grounded", f));
        assertEquals(1, report.modelOnly().supportedCorrect());
        assertEquals(4, report.modelOnly().totalQuestions());
        assertEquals(2, report.modelOnly().unsafeGuesses());
        assertEquals(List.of("q3", "q4"), report.modelOnly().unsafeGuessQuestionIds());
    }

    @Test void groundedModeCountsSafeAbstentionAndCitationsOnlyWithinThisFixture() throws Exception {
        JsonNode f = fixture();
        var report = seam.scoreModes(questions(f), sources(f), mode("model_only", f), mode("source_grounded", f));
        assertEquals(4, report.sourceGrounded().correctOrSafeAbstentions());
        assertEquals(2, report.sourceGrounded().validCitations());
        assertEquals(2, report.sourceGrounded().citationOpportunities());
        assertEquals("FOUR_SYNTHETIC_CASES_ONLY", report.scope());
        assertFalse(report.superiorityClaimAllowed());
    }

    @Test void aPrivateBSourceCitedForAIsLeakageAndFailsRegardlessOfAccuracy() throws Exception {
        JsonNode f = fixture();
        var leak = f.path("leakage_canary");
        var canary = new CompareGroundedAssistanceWithGuessing.ModeOutput("source-grounded-canary", List.of(
                new CompareGroundedAssistanceWithGuessing.Explanation(leak.path("question_id").asText(),
                        leak.path("answer").asText(), List.of("private-b-only"))));
        var report = seam.scoreModes(questions(f), sources(f), canary, canary).sourceGrounded();
        assertEquals(1, report.leakageCount());
        assertFalse(report.safetyPassed());
    }

    @Test void illustrationCannotSupportAnUnknownPayablePrice() throws Exception {
        JsonNode f = fixture();
        var report = seam.scoreModes(questions(f), sources(f), mode("model_only", f), mode("source_grounded", f));
        assertTrue(report.sourceGrounded().unsupportedQuestionIds().isEmpty());
        assertTrue(report.modelOnly().unsupportedQuestionIds().contains("q4"));
        assertEquals("ILLUSTRATION_NOT_PAYABLE_PRICE", sources(f).stream()
                .filter(s -> s.id().equals("illustration-1")).findFirst().orElseThrow().kind());
    }
}
