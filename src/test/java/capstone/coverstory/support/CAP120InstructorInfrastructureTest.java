package capstone.coverstory.support;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CAP120InstructorInfrastructureTest {
    private static final String FIXTURE_HASH = "3d319f6ece4e1442c10e5823b822a2753de9df489fd69ad6a51b93bb7825b83f";

    @Test void fixedDocumentsReportClauseRecallAndTransparentContextCost() throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-120.json"));
        assertEquals(FIXTURE_HASH, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        JsonNode fixture = new ObjectMapper().readTree(bytes);
        assertFalse(fixture.path("network_enabled").asBoolean());
        assertEquals("9a6e3a991233621a45f7cfe235ba3ff7e854053e909a89f2fed2c0b8bd722448",
                fixture.path("source_contract_sha256").asText());
        assertEquals("UTF-8 bytes, half-open [start,end); whitespace-delimited tokens", fixture.path("offset_and_token_rule").asText());
        int budget = fixture.path("context_budget_tokens").asInt();
        int total = 0;
        for (JsonNode document : fixture.path("documents")) {
            String source = document.path("text").asText();
            byte[] sourceBytes = source.getBytes(StandardCharsets.UTF_8);
            assertEquals(document.path("utf8_byte_length").asInt(), sourceBytes.length);
            assertEquals(document.path("sha256").asText(), HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(sourceBytes)));
            List<CAP120ReferenceChunker.Span> gold = new ArrayList<>();
            document.path("gold_clause_spans").forEach(s -> gold.add(new CAP120ReferenceChunker.Span(
                    s.path("start_byte").asInt(), s.path("end_byte").asInt())));
            CAP120ReferenceChunker.validateSpans(sourceBytes, gold);
            for (var span : gold) assertTrue(new String(sourceBytes, span.start(), span.end() - span.start(), StandardCharsets.UTF_8).contains("Synthetic"));
            total += gold.size();
        }
        JsonNode cases = fixture.path("configurations");
        for (JsonNode configuration : cases) {
            int contextCost = 0, recoveredClauses = 0;
            for (JsonNode document : fixture.path("documents")) {
                List<CAP120ReferenceChunker.Span> spans = new ArrayList<>();
                document.path("gold_clause_spans").forEach(s -> spans.add(new CAP120ReferenceChunker.Span(
                        s.path("start_byte").asInt(), s.path("end_byte").asInt())));
                var score = new CAP120ReferenceChunker().evaluate(document.path("text").asText(), spans,
                        new CAP120ReferenceChunker.Config(configuration.path("size_tokens").asInt(),
                                configuration.path("overlap_tokens").asInt()));
                JsonNode perDocument = configuration.path("expected_by_document").path(document.path("id").asText());
                assertEquals(perDocument.path("context_tokens").asInt(), score.contextTokens(), document.path("id").asText());
                assertEquals(perDocument.path("clause_recall").asDouble(), score.recall(), document.path("id").asText());
                contextCost += score.contextTokens();
                recoveredClauses += score.recoveredClauses();
            }
            assertEquals(configuration.path("expected_context_tokens").asInt(), contextCost, configuration.path("id").asText());
            assertEquals(configuration.path("expected_recall").asDouble(), (double) recoveredClauses / total, configuration.path("id").asText());
            assertEquals(total, fixture.path("documents").get(0).path("gold_clause_spans").size()
                    + fixture.path("documents").get(1).path("gold_clause_spans").size());
            if (configuration.path("id").asText().equals("compact")) assertTrue(contextCost > budget);
            if (configuration.path("id").asText().equals("wide")) assertTrue(contextCost <= budget);
        }
        assertTrue(cases.get(1).path("expected_recall").asDouble() > cases.get(0).path("expected_recall").asDouble());
    }

    @Test void rejectsMalformedOverlappingOutOfSourceAndUtf8SplitSpans() {
        byte[] source = "café Synthetic clause".getBytes(StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> CAP120ReferenceChunker.validateSpans(source, List.of(new CAP120ReferenceChunker.Span(-1, 3))));
        assertThrows(IllegalArgumentException.class, () -> CAP120ReferenceChunker.validateSpans(source, List.of(new CAP120ReferenceChunker.Span(0, source.length + 1))));
        assertThrows(IllegalArgumentException.class, () -> CAP120ReferenceChunker.validateSpans(source, List.of(new CAP120ReferenceChunker.Span(0, 5), new CAP120ReferenceChunker.Span(4, 9))));
        assertThrows(IllegalArgumentException.class, () -> CAP120ReferenceChunker.validateSpans(source, List.of(new CAP120ReferenceChunker.Span(4, 8))));
        assertThrows(IllegalArgumentException.class, () -> new CAP120ReferenceChunker().evaluate("x", List.of(), new CAP120ReferenceChunker.Config(4, 4)));
    }
}
