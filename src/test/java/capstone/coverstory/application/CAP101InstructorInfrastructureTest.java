package capstone.coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Independent reference witnesses. This suite never calls BoundAssistanceContext.buildContext. */
class CAP101InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-101/synthetic-v1.json");
    private record Chunk(String id, String owner, int tokens, String citation, String content) {}
    private record Plan(String status, List<String> included, List<String> omitted,
                        List<String> citations, int total, int outputReservation, boolean manual) {}

    private JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private List<Chunk> chunks(JsonNode root) {
        List<Chunk> rows = new ArrayList<>();
        for (JsonNode n : root.path("ranked_chunks")) rows.add(new Chunk(n.path("id").asText(), n.path("owner_id").asText(),
                n.path("tokens").asInt(), n.path("citation").asText(), n.path("content").asText()));
        return rows;
    }

    private static Plan reference(String owner, int base, int cap, int output, List<Chunk> ranked,
                                  java.util.function.ToIntFunction<Chunk> count) {
        if (base < 0 || cap < 0 || output < 0) throw new IllegalArgumentException("invalid budget");
        if (base > cap) return new Plan("INPUT_TOO_LARGE", List.of(), ranked.stream()
                .filter(c -> Objects.equals(owner, c.owner())).map(Chunk::id).toList(), List.of(), base, output, false);
        List<String> included = new ArrayList<>(), omitted = new ArrayList<>(), citations = new ArrayList<>();
        int used = base;
        for (Chunk chunk : ranked) {
            // Authorization happens before invoking the counter, which is the payload boundary.
            if (!Objects.equals(owner, chunk.owner())) continue;
            int tokens = count.applyAsInt(chunk);
            if (tokens < 0) throw new IllegalArgumentException("negative token count");
            if ((long) used + tokens <= cap) {
                used += tokens; included.add(chunk.id()); citations.add(chunk.citation());
            } else omitted.add(chunk.id());
        }
        if (included.isEmpty()) return new Plan("ABSTAIN", included, omitted, citations, used, output, true);
        return new Plan("READY", included, omitted, citations, used, output, false);
    }

    @Test void fixtureAndIndependentReferenceMatchSavedRankedChunkOutcome() throws Exception {
        JsonNode f = fixture();
        assertEquals("SYNTHETIC_FIXED_TOKEN_CONTEXT", f.path("classification").asText());
        assertEquals(0, f.path("provider_calls").asInt());
        assertEquals(4, f.path("ranked_chunks").size());
        List<Chunk> rows = chunks(f);
        Map<String, Integer> counts = new HashMap<>();
        rows.forEach(c -> counts.put(c.id(), c.tokens()));
        Plan plan = reference("owner-A", 300, 1500, 256, rows, c -> counts.get(c.id()));
        assertEquals("READY", plan.status());
        assertEquals(List.of("ai1", "ar1"), plan.included());
        assertEquals(List.of("a3"), plan.omitted(), "foreign source IDs are not returned in the plan");
        assertEquals(List.of("[ai1]", "[ar1]"), plan.citations());
        assertEquals(1200, plan.total());
        assertEquals(256, plan.outputReservation(), "output allowance is a separate reservation");
    }

    @Test void overlargeBaseReturnsBeforeAnyPayloadCounterInvocation() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        Plan p = reference("owner-A", 1501, 1500, 256, chunks(fixture()), c -> { calls.incrementAndGet(); return c.tokens(); });
        assertEquals("INPUT_TOO_LARGE", p.status());
        assertEquals(0, calls.get());
        assertFalse(p.omitted().contains("br1"), "foreign IDs stay out of the plan even in the early-return case");
    }

    @Test void foreignPrivateCanaryHasZeroCountAndZeroOutputBytes() throws Exception {
        Chunk foreign = chunks(fixture()).stream().filter(c -> c.id().equals("br1")).findFirst().orElseThrow();
        AtomicInteger counterCalls = new AtomicInteger();
        AtomicInteger foreignContentBytes = new AtomicInteger();
        Plan p = reference("owner-A", 300, 1500, 256, List.of(foreign), c -> {
            counterCalls.incrementAndGet();
            if (c.owner().equals("owner-B")) foreignContentBytes.addAndGet(c.content().getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
            return c.tokens();
        });
        assertEquals("ABSTAIN", p.status());
        assertTrue(p.manual());
        assertEquals(0, counterCalls.get());
        assertEquals(0, foreignContentBytes.get());
    }

    @Test void exactCapIncludesWholeChunkAndOneMoreTokenIsOmittedWhole() {
        List<Chunk> rows = List.of(new Chunk("ai1", "owner-A", 500, "[ai1]", "A1"),
                new Chunk("ar1", "owner-A", 400, "[ar1]", "A2"), new Chunk("exact", "owner-A", 300, "[exact]", "E"),
                new Chunk("overflow", "owner-A", 1, "[overflow]", "O"));
        Plan p = reference("owner-A", 300, 1500, 256, rows, Chunk::tokens);
        assertEquals(1500, p.total());
        assertEquals(List.of("ai1", "ar1", "exact"), p.included());
        assertEquals(List.of("overflow"), p.omitted());
        List<Chunk> partialWitness = List.of(rows.get(0), rows.get(1),
                new Chunk("too-large", "owner-A", 700, "[too-large]", "whole"));
        Plan noSplit = reference("owner-A", 300, 1500, 256, partialWitness, Chunk::tokens);
        assertEquals(List.of("ai1", "ar1"), noSplit.included());
        assertNotEquals(noSplit.included(), defectivePartialChunkSelection(partialWitness, 1500), "partial chunks must not appear in citations");
        assertNotEquals(p.included(), defectiveStrictBoundarySelection(rows, 1500), "the exact cap must remain inclusive");
    }

    @Test void allExcludedOrNonFittingSourcesAbstainToManualPath() {
        Plan unauthorized = reference("owner-A", 300, 1500, 256,
                List.of(new Chunk("br1", "owner-B", 90, "[br1]", "CANARY")), Chunk::tokens);
        Plan tooLargeWhole = reference("owner-A", 300, 1500, 256,
                List.of(new Chunk("large", "owner-A", 1500, "[large]", "whole")), Chunk::tokens);
        assertEquals("ABSTAIN", unauthorized.status()); assertTrue(unauthorized.manual());
        assertEquals("ABSTAIN", tooLargeWhole.status()); assertTrue(tooLargeWhole.manual());
        assertTrue(defectiveFallbackInventsAnswer(List.of()), "the defective fallback would fabricate an answer for an empty context");
    }

    private static List<String> defectivePartialChunkSelection(List<Chunk> rows, int cap) {
        List<String> included = new ArrayList<>(); int used = 300;
        for (Chunk c : rows) {
            int remaining = cap - used;
            if (c.tokens() <= remaining) { included.add(c.id()); used += c.tokens(); }
            else if (remaining > 0) { included.add(c.id()); used = cap; }
        }
        return included;
    }
    private static List<String> defectiveStrictBoundarySelection(List<Chunk> rows, int cap) {
        List<String> included = new ArrayList<>(); int used = 300;
        for (Chunk c : rows) if ((long) used + c.tokens() < cap) { included.add(c.id()); used += c.tokens(); }
        return included;
    }
    private static boolean defectiveFallbackInventsAnswer(List<Chunk> candidates) { return candidates.isEmpty(); }
}
