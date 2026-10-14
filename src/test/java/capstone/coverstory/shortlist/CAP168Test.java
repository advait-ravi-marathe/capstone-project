package capstone.coverstory.shortlist;

import capstone.coverstory.shortlist.api.CAP168Seam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Learner-owned parser acceptance tests; login/quote service flow is not tested here. */
@Tag("learnerAcceptance")
class CAP168Test {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-168/synthetic-v1.json");

    @Test void savedFiveLinesReturnValidLinesOneAndThreeAndSafeRejectionsTwoAndFive() throws Exception {
        CAP168Seam.Classification result = new CAP168Seam().classifyQuoteReviewLines(savedLines());
        assertEquals(List.of(1,3), result.validRecords().stream().map(CAP168Seam.QuoteReviewRecord::sourceLine).toList());
        assertEquals(List.of(2,5), result.rejections().stream().map(CAP168Seam.SafeRejection::lineNumber).toList());
        assertEquals(1, result.blankLinesSkipped());
        assertFalse(result.complete());
    }

    @Test void integerPaiseInrAndSourceVersionArePreserved() throws Exception {
        var rows = new CAP168Seam().classifyQuoteReviewLines(savedLines()).validRecords();
        assertEquals(List.of(100000L,25100L), rows.stream().map(CAP168Seam.QuoteReviewRecord::totalPaise).toList());
        assertEquals(List.of("v3","v1"), rows.stream().map(CAP168Seam.QuoteReviewRecord::sourceVersion).toList());
        assertTrue(rows.stream().allMatch(row -> "INR".equals(row.currency()) && !row.sourceIllustration()));
    }

    @Test void malformedSyntheticTokenNeverAppearsInSafeDiagnostics() throws Exception {
        var result = new CAP168Seam().classifyQuoteReviewLines(savedLines());
        assertFalse(result.rejections().toString().contains("synthetic-token-CAP168"));
    }

    private static List<String> savedLines() throws Exception {
        JsonNode node = JSON.readTree(Files.readString(FIXTURE));
        List<String> lines = new ArrayList<>(); node.path("source_lines").forEach(value -> lines.add(value.asText()));
        return lines;
    }
}
