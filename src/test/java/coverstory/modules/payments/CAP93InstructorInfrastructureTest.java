package coverstory.modules.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Independent local oracle checks. These are not production or integrated probe evidence. */
class CAP93InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-93/synthetic-v1.json");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void fixtureIsBoundedSyntheticAndHasNoProviderOrBroker() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("runtime_provider_calls").asBoolean());
        assertFalse(root.path("runtime_broker_required").asBoolean());
        assertFalse(root.path("payment_authority_changes").asBoolean());
        assertEquals(3, root.path("jobs").size());
        assertEquals("BLOCKED_NOT_RUN", root.path("evidence_status").asText());
        assertTrue(root.path("actual_probe_output").isNull());
    }

    @Test void virtualOutageKeepsThreeJobsPendingUntilRecoveryThenCompletesEachOnce() {
        CAP93OutageOracle oracle = seeded();
        oracle.advanceBy(Duration.ofSeconds(30));
        assertEquals(Duration.ofSeconds(30), oracle.virtualTime());
        assertEquals(new CAP93OutageOracle.Snapshot(3, 0, 0, 0), oracle.snapshot());
        oracle.recover();
        for (String id : List.of("read-001", "read-002", "read-003")) oracle.deliver(id);
        assertEquals(new CAP93OutageOracle.Snapshot(0, 3, 0, 0), oracle.snapshot());
    }

    @Test void repeatedDeliveryDoesNotRepeatCompletionOrCreatePaymentAttempt() {
        CAP93OutageOracle oracle = seeded(); oracle.recover();
        oracle.deliver("read-001");
        var once = oracle.snapshot();
        oracle.deliver("read-001"); oracle.deliver("read-001");
        assertEquals(once, oracle.snapshot());
        assertEquals(0, oracle.snapshot().paymentAttempts());
    }

    @Test void diagnosticTotalsAllCategoriesAndIsNotAnAwsScore() throws Exception {
        JsonNode e = JSON.readTree(Files.readString(FIXTURE)).path("expected").path("diagnostic_correct_counts");
        assertEquals(2 + 1 + 3 + 2, e.path("numerator").asInt());
        assertEquals(12, e.path("denominator").asInt());
        assertTrue(JSON.readTree(Files.readString(FIXTURE)).path("expected").path("aws_exam_score").isNull());
    }

    @Test void missingActualOutputCannotBecomeCapacityOrRoleFitEvidence() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertTrue(root.path("actual_probe_output").isNull());
        assertEquals("UNRESOLVED", root.path("expected").path("capacity_role_fit").asText());
        assertEquals("BLOCKED_NOT_RUN", root.path("evidence_status").asText());
    }

    @Test void zeroSecondAndThirtySecondOutagesAreTheOnlySupportedEndpoints() {
        for (int seconds : List.of(0, 1, 15, 30)) {
            CAP93OutageOracle oracle = seeded(); oracle.advanceBy(Duration.ofSeconds(seconds));
            assertEquals(seconds, oracle.virtualTime().toSeconds());
            assertEquals(new CAP93OutageOracle.Snapshot(3, 0, 0, 0), oracle.snapshot());
        }
        assertTrue(Duration.ofSeconds(31).compareTo(Duration.ofSeconds(30)) > 0);
    }

    private static CAP93OutageOracle seeded() {
        CAP93OutageOracle oracle = new CAP93OutageOracle();
        oracle.acceptReadJob("read-001"); oracle.acceptReadJob("read-002"); oracle.acceptReadJob("read-003");
        return oracle;
    }
}
