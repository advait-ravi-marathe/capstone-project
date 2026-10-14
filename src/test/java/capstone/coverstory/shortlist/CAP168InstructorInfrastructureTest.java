package capstone.coverstory.shortlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Independent parser oracle; no login, quotation, journey, or ordering service is simulated. */
class CAP168InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-168/synthetic-v1.json");
    private static final Set<String> FIELDS = Set.of("quoteId", "scenarioId", "sourceId", "sourceVersion", "termsHash", "totalPaise", "currency", "sourceIllustration");
    private static final Pattern HASH = Pattern.compile("[0-9a-f]{64}");

    @Test void savedFivePhysicalLinesClassifyWithOneBasedLineageAndRedactedErrors() throws Exception {
        JsonNode f = fixture();
        List<String> lines = new ArrayList<>(); f.path("source_lines").forEach(line -> lines.add(line.asText()));
        Report report = reference(lines);
        assertEquals(List.of(1, 3), report.valid.stream().map(row -> row.line).toList());
        assertEquals(List.of(2, 5), report.rejections.stream().map(SafeRejection::lineNumber).toList());
        assertEquals(List.of(4), report.blankLines);
        assertFalse(report.complete);
        assertFalse(report.toString().contains("synthetic-token-CAP168"));
        assertFalse(report.rejections.toString().contains("synthetic-token-CAP168"));
        assertEquals("MALFORMED_JSON", report.rejections.get(0).code);
        assertEquals("MISSING_TERMS_HASH", report.rejections.get(1).code);
    }

    @Test void exactIntegerInrQuotesRetainSourceTermsAndRevisionFields() throws Exception {
        JsonNode f = fixture(); List<String> lines = new ArrayList<>(); f.path("source_lines").forEach(n -> lines.add(n.asText()));
        Report report = reference(lines);
        assertEquals(List.of("quote-A", "quote-B"), report.valid.stream().map(row -> row.quoteId).toList());
        assertEquals(List.of(100000L, 25100L), report.valid.stream().map(row -> row.totalPaise).toList());
        assertTrue(report.valid.stream().allMatch(row -> "INR".equals(row.currency) && !row.sourceIllustration));
        assertEquals(List.of("v3", "v1"), report.valid.stream().map(row -> row.sourceVersion).toList());
    }

    @Test void decimalAmountsUnknownFieldsIllustrationsAndMissingTermsFailClosed() {
        List<String> variants = List.of(
                "{\"quoteId\":\"q\",\"scenarioId\":\"s\",\"sourceId\":\"a\",\"sourceVersion\":\"v1\",\"termsHash\":\""+"a".repeat(64)+"\",\"totalPaise\":100.5,\"currency\":\"INR\",\"sourceIllustration\":false}",
                "{\"quoteId\":\"q\",\"scenarioId\":\"s\",\"sourceId\":\"a\",\"sourceVersion\":\"v1\",\"termsHash\":\""+"a".repeat(64)+"\",\"totalPaise\":100,\"currency\":\"INR\",\"sourceIllustration\":false,\"email\":\"synthetic@example.invalid\"}",
                "{\"quoteId\":\"q\",\"scenarioId\":\"s\",\"sourceId\":\"a\",\"sourceVersion\":\"v1\",\"termsHash\":\""+"a".repeat(64)+"\",\"totalPaise\":100,\"currency\":\"INR\",\"sourceIllustration\":true}"
        );
        Report report = reference(variants);
        assertEquals(List.of(1, 2, 3), report.rejections.stream().map(SafeRejection::lineNumber).toList());
        assertTrue(report.valid.isEmpty());
        assertFalse(report.toString().contains("synthetic@example.invalid"));
    }

    @Test void fixtureRetainsTheUnimplementedFullFlowContractsAsDeferredCases() throws Exception {
        JsonNode cases = fixture().path("deferred_full_flow_cases");
        assertEquals(6, cases.size());
        assertEquals("CAP-168-CS-07", cases.get(5).path("id").asText());
        assertTrue(cases.toString().contains("late owner-A response"));
    }

    private static Report reference(List<String> lines) {
        List<ValidQuote> valid = new ArrayList<>(); List<SafeRejection> rejected = new ArrayList<>(); List<Integer> blank = new ArrayList<>();
        for (int i=0; i<lines.size(); i++) {
            int line = i+1; String raw=lines.get(i);
            if (raw.isBlank()) { blank.add(line); continue; }
            JsonNode node;
            try { node=JSON.readTree(raw); } catch (Exception invalid) { rejected.add(new SafeRejection(line,"MALFORMED_JSON")); continue; }
            if (node==null || !node.isObject()) { rejected.add(new SafeRejection(line,"INVALID_RECORD")); continue; }
            Set<String> names = new HashSet<>(); node.fieldNames().forEachRemaining(names::add);
            if (!names.equals(FIELDS)) { rejected.add(new SafeRejection(line,names.contains("termsHash") ? "UNKNOWN_OR_MISSING_FIELD" : "MISSING_TERMS_HASH")); continue; }
            JsonNode total=node.path("totalPaise"), terms=node.path("termsHash");
            if (!total.isIntegralNumber() || total.asLong() <= 0) { rejected.add(new SafeRejection(line,"INVALID_TOTAL_PAISE")); continue; }
            if (!HASH.matcher(terms.asText()).matches()) { rejected.add(new SafeRejection(line,"INVALID_TERMS_HASH")); continue; }
            if (!"INR".equals(node.path("currency").asText())) { rejected.add(new SafeRejection(line,"UNKNOWN_CURRENCY")); continue; }
            if (node.path("sourceIllustration").asBoolean()) { rejected.add(new SafeRejection(line,"ILLUSTRATION_NOT_PAYABLE")); continue; }
            if (!node.path("quoteId").isTextual() || !node.path("scenarioId").isTextual()
                    || !node.path("sourceId").isTextual() || !node.path("sourceVersion").isTextual()) {
                rejected.add(new SafeRejection(line,"MISSING_SOURCE_BINDING")); continue;
            }
            valid.add(new ValidQuote(line,node.path("quoteId").asText(),node.path("scenarioId").asText(),node.path("sourceId").asText(),
                    node.path("sourceVersion").asText(),terms.asText(),total.asLong(),"INR",false));
        }
        return new Report(valid,rejected,blank,rejected.isEmpty());
    }
    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private record ValidQuote(int line,String quoteId,String scenarioId,String sourceId,String sourceVersion,String termsHash,long totalPaise,String currency,boolean sourceIllustration) { }
    private record SafeRejection(int lineNumber,String code) { }
    private record Report(List<ValidQuote> valid,List<SafeRejection> rejections,List<Integer> blankLines,boolean complete) { }
}
