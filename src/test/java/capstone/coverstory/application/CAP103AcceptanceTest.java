package capstone.coverstory.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Learner-facing acceptance contract. These tests intentionally exercise the TODO seam. */
@Tag("learnerAcceptance")
class CAP103AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-103/synthetic-v1.json");
    private final MeasureHonestOnboardingRequestLatency seam = new MeasureHonestOnboardingRequestLatency();

    @Test void tenSuccessesUseNearestRankPercentiles() throws Exception {
        var report = seam.summarizeLatency(readRequests("CAP-103-CS-01"));
        assertEquals(10, report.successCount());
        assertEquals(50L, report.p50Millis());
        assertEquals(100L, report.p95Millis());
        assertEquals(100L, report.maxSuccessMillis());
    }

    @Test void errorsRemainInTotalAndAreSeparateFromSuccessStatistics() throws Exception {
        var report = seam.summarizeLatency(readRequests("CAP-103-CS-02"));
        assertEquals(12, report.totalRequests());
        assertEquals(10, report.successCount());
        assertEquals(2, report.errorCount());
        assertEquals(50L, report.p50Millis());
        assertEquals(100L, report.p95Millis());
        assertEquals(500L, report.errorMaxMillis());
    }

    @Test void invalidDurationIsDiagnosedAndExcluded() throws Exception {
        var report = seam.summarizeLatency(readRequests("CAP-103-CS-03"));
        assertEquals(2, report.totalRequests());
        assertEquals(1, report.successCount());
        assertEquals(List.of("INVALID_DURATION"), report.diagnostics());
        assertEquals(10L, report.p50Millis());
    }

    @Test void emptySuccessPopulationHasNullPercentiles() throws Exception {
        var report = seam.summarizeLatency(readRequests("CAP-103-CS-04"));
        assertNull(report.p50Millis());
        assertNull(report.p95Millis());
        assertNull(report.maxSuccessMillis());
        assertEquals(2, report.errorCount());
    }

    @Test void resultLabelsOnlyLocalSyntheticConditions() throws Exception {
        var report = seam.summarizeLatency(readRequests("CAP-103-CS-01"));
        assertEquals("LOCAL_SYNTHETIC", report.runLabel());
        assertFalse(report.capacityClaim());
        assertFalse(report.availabilityClaim());
    }

    private static List<MeasureHonestOnboardingRequestLatency.MeasuredRequest> readRequests(String caseId) throws Exception {
        for (JsonNode row : JSON.readTree(Files.readString(FIXTURE)).path("cases")) {
            if (!caseId.equals(row.path("id").asText())) continue;
            List<MeasureHonestOnboardingRequestLatency.MeasuredRequest> requests = new ArrayList<>();
            for (JsonNode request : row.path("requests")) {
                requests.add(new MeasureHonestOnboardingRequestLatency.MeasuredRequest(
                        request.path("start_ms").asLong(), request.path("end_ms").asLong(),
                        MeasureHonestOnboardingRequestLatency.Status.valueOf(request.path("status").asText())));
            }
            return requests;
        }
        throw new IllegalArgumentException("Fixture case not found: " + caseId);
    }
}
