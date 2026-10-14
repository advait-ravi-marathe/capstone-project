package coverstory.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Independent fixture oracle; does not invoke the learner seam or any model/provider. */
class CAP97InstructorInfrastructureTest {
    private final ObjectMapper json = new ObjectMapper();
    private JsonNode fixture() throws Exception {
        return json.readTree(Files.readString(Path.of("fixtures/coverstory/CAP-97/synthetic-v1.json")));
    }

    private record RefReport(int supportedCorrect, int total, int unsafeGuesses, int safeCount,
                             int validCitations, int citationOpportunities, int leakageCount,
                             boolean safe, List<String> unsupported, List<String> unsafeIds,
                             List<String> leakingIds) { }

    private RefReport reference(JsonNode root, String modeName) {
        Map<String, JsonNode> sources = new HashMap<>(); root.path("sources").forEach(s -> sources.put(s.path("id").asText(), s));
        Map<String, JsonNode> answers = new HashMap<>(); root.path(modeName).forEach(a -> answers.put(a.path("question_id").asText(), a));
        int correct = 0, unsafe = 0, safeCount = 0, valid = 0, opportunities = 0, leakage = 0;
        List<String> unsupported = new ArrayList<>(), unsafeIds = new ArrayList<>(), leaking = new ArrayList<>();
        for (JsonNode q : root.path("questions")) {
            String id = q.path("id").asText(), owner = q.path("owner_id").asText();
            JsonNode a = answers.get(id); String answer = a == null || a.path("answer").isNull() ? null : a.path("answer").asText();
            boolean answerable = q.path("answerable").asBoolean();
            boolean exact = answerable && answer != null && answer.equals(q.path("supported_answer").asText());
            if (exact) correct++;
            if (answerable) opportunities++;
            boolean hasValidCitation = false;
            for (JsonNode citation : (a == null ? json.createArrayNode() : a.path("citations"))) {
                JsonNode source = sources.get(citation.asText());
                if (source == null) { unsupported.add(id); continue; }
                String sourceOwner = source.path("owner_id").asText();
                if (!sourceOwner.equals("PUBLIC") && !sourceOwner.equals(owner)) { leakage++; if (!leaking.contains(id)) leaking.add(id); }
                boolean permitted = false;
                for (JsonNode p : q.path("permitted_source_ids")) if (p.asText().equals(citation.asText())) permitted = true;
                boolean supports = false;
                for (JsonNode supportedId : source.path("supports_question_ids")) if (supportedId.asText().equals(id)) supports = true;
                if (permitted && supports && (sourceOwner.equals("PUBLIC") || sourceOwner.equals(owner))) hasValidCitation = true;
                else if (answer != null && !unsupported.contains(id)) unsupported.add(id);
            }
            if (answerable && hasValidCitation) valid++;
            if (!answerable && answer != null) { unsafe++; unsafeIds.add(id); }
            if ((answerable && exact) || (!answerable && answer == null)) safeCount++;
            if (answerable && answer != null && !exact && !unsupported.contains(id)) unsupported.add(id);
            if (!answerable && answer != null && !unsupported.contains(id)) unsupported.add(id);
        }
        return new RefReport(correct, root.path("questions").size(), unsafe, safeCount, valid,
                opportunities, leakage, leakage == 0 && unsafe == 0, unsupported, unsafeIds, leaking);
    }

    private void matches(JsonNode expected, RefReport actual) {
        assertEquals(expected.path("supported_correct").asInt(), actual.supportedCorrect());
        assertEquals(expected.path("total_questions").asInt(), actual.total());
        assertEquals(expected.path("unsafe_guesses").asInt(), actual.unsafeGuesses());
        assertEquals(expected.path("correct_or_safe_abstentions").asInt(), actual.safeCount());
        assertEquals(expected.path("valid_citations").asInt(), actual.validCitations());
        assertEquals(expected.path("citation_opportunities").asInt(), actual.citationOpportunities());
        assertEquals(expected.path("leakage_count").asInt(), actual.leakageCount());
        assertEquals(expected.path("safety_passed").asBoolean(), actual.safe());
        assertEquals(strings(expected.path("unsupported_question_ids")), actual.unsupported());
        assertEquals(strings(expected.path("unsafe_guess_question_ids")), actual.unsafeIds());
    }
    private List<String> strings(JsonNode values) { List<String> out = new ArrayList<>(); values.forEach(v -> out.add(v.asText())); return out; }

    @Test void fixedOutputsMatchExpectedReportAndRetainTheTwoUnsafeGuesses() throws Exception {
        JsonNode f = fixture(); assertTrue(f.path("synthetic").asBoolean()); assertEquals(0, f.path("model_or_provider_calls").asInt());
        matches(f.at("/expected/model_only"), reference(f, "model_only"));
        matches(f.at("/expected/source_grounded"), reference(f, "source_grounded"));
        assertEquals(1, reference(f, "model_only").supportedCorrect());
        assertEquals(2, reference(f, "model_only").unsafeGuesses());
        assertEquals(4, reference(f, "source_grounded").safeCount());
        assertEquals(2, reference(f, "source_grounded").validCitations());
    }

    @Test void ownerBlindScorerWitnessStillFailsSafetyEvenWhenAnswerIsCorrect() throws Exception {
        JsonNode f = fixture(), canary = f.path("leakage_canary");
        // Replace q1's correct public citation with a private B-only source.
        ObjectNode canaryFixture = ((ObjectNode) f).deepCopy();
        ArrayNode canaryRows = json.createArrayNode();
        canaryRows.addObject().put("question_id", canary.path("question_id").asText())
                .put("answer", canary.path("answer").asText()).set("citations", json.createArrayNode().add("private-b-only"));
        canaryFixture.set("source_grounded", canaryRows);
        RefReport correct = reference(canaryFixture, "source_grounded");
        // Deliberately defective variant only checks the exact answer and ignores owner scope.
        boolean unsafeMutantWouldPass = !canary.path("answer").asText().isBlank();
        assertTrue(unsafeMutantWouldPass);
        assertEquals(1, correct.leakageCount());
        assertFalse(correct.safe());
        JsonNode source = null;
        for (JsonNode row : f.path("sources")) if (row.path("id").asText().equals("private-b-only")) source = row;
        assertNotNull(source);
        assertEquals("B", source.path("owner_id").asText());
        assertEquals(1, f.at("/expected/leakage_canary/leakage_count").asInt());
        assertFalse(f.at("/expected/leakage_canary/safety_passed").asBoolean());
    }

    @Test void illustrationIsNotLabeledAsSupportForPayablePriceAndScopeDisallowsSuperiority() throws Exception {
        JsonNode f = fixture();
        JsonNode illustration = null;
        for (JsonNode row : f.path("sources")) if (row.path("id").asText().equals("illustration-1")) illustration = row;
        assertNotNull(illustration);
        assertEquals("ILLUSTRATION_NOT_PAYABLE_PRICE", illustration.path("kind").asText());
        assertEquals(0, illustration.path("supports_question_ids").size());
        assertFalse(f.at("/expected/claim_rules/illustration_price_supported").asBoolean());
        assertFalse(f.at("/expected/claim_rules/superiority_claim_allowed").asBoolean());
    }
}
