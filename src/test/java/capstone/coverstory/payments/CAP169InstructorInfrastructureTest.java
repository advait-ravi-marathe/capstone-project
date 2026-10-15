package capstone.coverstory.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Independent saved-fixture oracle; it does not call the learner seam. */
class CAP169InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-169/synthetic-v1.json");
    private static final Pattern WHOLE_SECONDS = Pattern.compile("[0-9]+");

    @Test void fixtureIsOfflineAndCarriesTheSavedContractCases() throws Exception {
        JsonNode root = fixture();
        assertEquals("SYNTHETIC_OFFLINE_RETRY_AFTER_PARSE_ONLY", root.path("classification").asText());
        assertFalse(root.path("actual_provider_call").asBoolean());
        assertEquals(30, root.path("max_wait_seconds").asInt());
        assertEquals(5, root.path("cases").size());
    }

    @Test void independentParserMatchesEverySavedExpectedClassification() throws Exception {
        JsonNode root = fixture();
        Clock clock = Clock.fixed(Instant.parse(root.path("fixed_clock").asText()), java.time.ZoneOffset.UTC);
        Duration max = Duration.ofSeconds(root.path("max_wait_seconds").asLong());
        for (JsonNode row : root.path("cases")) {
            if (row.has("input")) {
                JsonNode expected = row.path("expected");
                OracleDecision actual = reference(row.path("input").path("header").asText(), clock, max);
                assertEquals(expected.path("status").asText(), actual.status, row.path("id").asText());
                assertEquals(expected.path("wait_seconds").isNull() ? null : expected.path("wait_seconds").asLong(), actual.waitSeconds);
            } else {
                for (JsonNode invalid : row.path("invalid_headers"))
                    assertEquals("INVALID_HEADER", reference(invalid.asText(), clock, max).status);
                assertEquals("ABSENT", reference(null, clock, max).status);
            }
        }
    }

    @Test void boundaryAndMalformedWitnessesRejectEarlyRetryAndClamping() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), java.time.ZoneOffset.UTC);
        Duration cap = Duration.ofSeconds(30);
        assertEquals(new OracleDecision("STOP_LONG_DELAY", null), reference("31", clock, cap));
        assertEquals(new OracleDecision("WAIT", 30L), reference("30", clock, cap));
        assertEquals(new OracleDecision("WAIT", 0L), reference("Wed, 14 Oct 2026 11:00:00 GMT", clock, cap));
        assertEquals("INVALID_HEADER", reference("+10", clock, cap).status);
        assertEquals("INVALID_HEADER", reference("1.0", clock, cap).status);
    }

    private static OracleDecision reference(String raw, Clock clock, Duration cap) {
        if (raw == null) return new OracleDecision("ABSENT", null);
        Duration wait;
        if (WHOLE_SECONDS.matcher(raw).matches()) {
            try { wait = Duration.ofSeconds(Long.parseLong(raw)); }
            catch (ArithmeticException | NumberFormatException invalid) { return new OracleDecision("INVALID_HEADER", null); }
        } else {
            try {
                Instant target = ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                Duration delta = Duration.between(clock.instant(), target);
                wait = delta.isNegative() ? Duration.ZERO : delta;
            } catch (RuntimeException invalid) {
                return new OracleDecision("INVALID_HEADER", null);
            }
        }
        return wait.compareTo(cap) > 0
                ? new OracleDecision("STOP_LONG_DELAY", null)
                : new OracleDecision("WAIT", wait.getSeconds());
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private record OracleDecision(String status, Long waitSeconds) { }
}
