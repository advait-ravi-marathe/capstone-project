package capstone.coverstory.support;

import capstone.coverstory.support.internal.ClauseChunker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance tests. The fixed reference oracle is tested separately. */
@Tag("learnerAcceptance")
class ClauseChunkerTest {
    @Test void configurationsMatchBothFrozenDocumentsRecallAndContextCost() throws Exception {
        JsonNode fixture = fixture();
        for (JsonNode configuration : fixture.path("configurations")) {
            int totalContext = 0;
            int recovered = 0;
            int totalClauses = 0;
            for (JsonNode document : fixture.path("documents")) {
                String source = document.path("text").asText();
                List<ClauseChunker.Span> clauses = spans(document);
                var result = ClauseChunker.chunk(source, clauses, new ClauseChunker.Config(
                        configuration.path("size_tokens").asInt(), configuration.path("overlap_tokens").asInt()));
                JsonNode expected = configuration.path("expected_by_document").path(document.path("id").asText());
                assertEquals(expected.path("context_tokens").asInt(), result.contextTokens(), document.path("id").asText());
                assertEquals(expected.path("clause_recall").asDouble(), result.clauseRecall(), document.path("id").asText());
                totalContext += result.contextTokens();
                recovered += (int) Math.round(result.clauseRecall() * clauses.size());
                totalClauses += clauses.size();
            }
            assertEquals(configuration.path("expected_context_tokens").asInt(), totalContext);
            assertEquals(configuration.path("expected_recall").asDouble(), (double) recovered / totalClauses);
        }
    }

    @Test void wideConfigurationKeepsTheHeadingAndBodyClauseWithinOneWindow() throws Exception {
        JsonNode fixture = fixture();
        JsonNode document = fixture.path("documents").get(0);
        JsonNode configuration = fixture.path("configurations").get(1);
        String source = document.path("text").asText();
        var span = spans(document).get(0);
        var result = ClauseChunker.chunk(source, spans(document), new ClauseChunker.Config(
                configuration.path("size_tokens").asInt(), configuration.path("overlap_tokens").asInt()));
        assertTrue(result.chunks().stream().anyMatch(c -> c.startByte() <= span.startByte() && c.endByte() >= span.endByte()));
    }

    @Test void invalidAndOverlappingSpansAreRejectedBeforeChunking() {
        String source = "Synthetic café claim text";
        assertThrows(IllegalArgumentException.class, () -> ClauseChunker.chunk(source,
                List.of(new ClauseChunker.Span(-1, 3)), new ClauseChunker.Config(8, 2)));
        assertThrows(IllegalArgumentException.class, () -> ClauseChunker.chunk(source,
                List.of(new ClauseChunker.Span(0, 12), new ClauseChunker.Span(10, 20)), new ClauseChunker.Config(8, 2)));
        assertThrows(IllegalArgumentException.class, () -> ClauseChunker.chunk(source,
                List.of(), new ClauseChunker.Config(4, 4)));
    }

    private static JsonNode fixture() throws Exception {
        return new ObjectMapper().readTree(Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-120.json")));
    }

    private static List<ClauseChunker.Span> spans(JsonNode document) {
        java.util.ArrayList<ClauseChunker.Span> result = new java.util.ArrayList<>();
        document.path("gold_clause_spans").forEach(span -> result.add(new ClauseChunker.Span(
                span.path("start_byte").asInt(), span.path("end_byte").asInt())));
        return List.copyOf(result);
    }
}
