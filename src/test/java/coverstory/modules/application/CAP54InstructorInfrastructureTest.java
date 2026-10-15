package coverstory.modules.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.application.api.CAP54Seam.ApplicationStateReader;
import coverstory.modules.application.api.CAP54Seam.AuthoritativeState;
import coverstory.modules.application.api.CAP54Seam.JourneyLink;
import coverstory.modules.application.api.CAP54Seam.OwnedJourneyReader;
import coverstory.modules.application.api.CAP54Seam.Page;
import coverstory.modules.application.api.CAP54Seam.SafeSummary;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-only fixture/oracle checks; no learner seam is invoked here. */
class CAP54InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-54/synthetic-v1.json");

    @Test void fixtureHasOwnerBoundJourneyRevisionsAndSafeOutputVectors() throws Exception {
        JsonNode root = new ObjectMapper().readTree(Files.readString(FIXTURE));
        assertEquals("CAP-54-synthetic-v1", root.path("fixture_id").asText());
        assertEquals("SYNTHETIC_ONLY", root.path("classification").asText());
        assertEquals(2, root.path("journeys").size());
        assertEquals(8, root.path("cases").size());
        for (JsonNode row : root.path("journeys")) {
            UUID.fromString(row.path("journey_id").asText());
            UUID.fromString(row.path("application_id").asText());
            assertTrue(row.path("revision").asLong() > 0);
            assertTrue(row.path("private_payload_marker").asText().startsWith("SYNTHETIC_PRIVATE_SENTINEL_"));
        }
        assertEquals("user-A", root.path("journeys").get(0).path("owner_id").asText());
        assertEquals("PAYMENT_PENDING", root.path("journeys").get(0).path("authoritative_status").asText());
    }

    @Test void accessOraclePassesOwnJourneyAndRejectsUuidOnlyAuthorizationDefect() throws Exception {
        JsonNode root = new ObjectMapper().readTree(Files.readString(FIXTURE));
        Map<String, JsonNode> byId = new HashMap<>();
        root.path("journeys").forEach(row -> byId.put(row.path("journey_id").asText(), row));
        String journeyA = "3c7d76dc-29dd-4f2c-a979-8c4a40c8a001";
        Map<String, Object> owned = referenceResume("user-A", journeyA, byId);
        assertEquals(Map.of("status", 200, "journeyId", journeyA,
                "businessStatus", "PAYMENT_PENDING", "resumeAction", "CONTINUE_CHECKOUT"), owned);

        Map<String, Object> foreign = referenceResume("user-B", journeyA, byId);
        Map<String, Object> absent = referenceResume("user-A", "00000000-0000-4000-8000-000000000099", byId);
        assertEquals(Map.of("status", 404, "error", "NOT_FOUND"), foreign);
        assertEquals(absent, foreign, "foreign and missing must be indistinguishable");
        assertFalse(unsafeUuidOnlyLookup(journeyA, byId).equals(foreign),
                "the witness must detect lookup-by-UUID without owner scope");
        assertFalse(owned.toString().contains("SYNTHETIC_PRIVATE_SENTINEL"));
    }

    @Test void cursorOracleAcceptsOnlyBoundedOwnerMatchedPages() {
        assertTrue(validListRequest("user-A", 50, "cursor:user-A"));
        assertFalse(validListRequest("user-A", 51, "cursor:user-A"));
        assertFalse(validListRequest("user-A", 50, "cursor:user-B"));
        assertFalse(validListRequest("user-A", 0, null));
    }

    @Test void fixtureAdapterUsesApplicationModulePortsAndExactAuthoritativeRevision() throws Exception {
        FixturePorts ports = fixturePorts();
        UUID journey = UUID.fromString("3c7d76dc-29dd-4f2c-a979-8c4a40c8a001");
        Optional<JourneyLink> owned = ports.journeys().findOwned("user-A", journey);
        assertTrue(owned.isPresent());
        assertEquals(2, owned.orElseThrow().revision());
        assertEquals(Optional.empty(), ports.journeys().findOwned("user-B", journey));
        AuthoritativeState state = ports.states().load(owned.orElseThrow().applicationId(), owned.orElseThrow().revision()).orElseThrow();
        assertEquals("PAYMENT_PENDING", state.status());
        assertEquals("CONTINUE_CHECKOUT", actionFor(state));
        assertEquals(1, ports.stateReads().get());
        assertFalse(state.toString().contains("SYNTHETIC_PRIVATE_SENTINEL"));
    }

    @Test void portAdapterRejectsForeignAndMissingIdenticallyBeforeApplicationRead() throws Exception {
        FixturePorts ports = fixturePorts();
        UUID existingForeignJourney = UUID.fromString("3c7d76dc-29dd-4f2c-a979-8c4a40c8a001");
        UUID missingJourney = UUID.fromString("00000000-0000-4000-8000-000000000099");
        Optional<JourneyLink> foreign = ports.journeys().findOwned("user-B", existingForeignJourney);
        Optional<JourneyLink> missing = ports.journeys().findOwned("user-B", missingJourney);
        assertEquals(missing, foreign);
        assertEquals(0, ports.stateReads().get());
        assertEquals(404, safeStatus(foreign));
        assertEquals(404, safeStatus(missing));
    }

    @Test void ownedListPortReturnsOnlySafeSummariesAndChecksCursorAndLimit() throws Exception {
        FixturePorts ports = fixturePorts();
        assertTrue(validListRequest("user-A", 20, null));
        assertEquals(1, ports.journeys().listOwned("user-A", 20, null).journeys().size());
        assertTrue(ports.journeys().listOwned("user-A", 20, null).journeys().stream()
                .noneMatch(summary -> summary.toString().contains("SYNTHETIC_PRIVATE_SENTINEL")));
        assertFalse(validListRequest("user-A", 51, "cursor:user-A"));
        assertFalse(validListRequest("user-A", 20, "cursor:user-B"));
    }

    private static int safeStatus(Optional<JourneyLink> link) { return link.isPresent() ? 200 : 404; }

    private static String actionFor(AuthoritativeState state) {
        return state.dispatchConfirmed() ? "VIEW_POLICY"
                : state.paymentConfirmed() ? "WAIT_FOR_DISPATCH" : "CONTINUE_CHECKOUT";
    }

    private static FixturePorts fixturePorts() throws Exception {
        JsonNode root = new ObjectMapper().readTree(Files.readString(FIXTURE));
        Map<UUID, FixtureJourney> rows = new HashMap<>();
        Map<String, AuthoritativeState> states = new HashMap<>();
        root.path("journeys").forEach(row -> {
            UUID journeyId = UUID.fromString(row.path("journey_id").asText());
            UUID applicationId = UUID.fromString(row.path("application_id").asText());
            long revision = row.path("revision").asLong();
            String owner = row.path("owner_id").asText();
            rows.put(journeyId, new FixtureJourney(owner, new JourneyLink(journeyId, applicationId, revision)));
            states.put(stateKey(applicationId, revision), new AuthoritativeState(row.path("authoritative_status").asText(),
                    row.path("payment_confirmed").asBoolean(), row.path("dispatch_confirmed").asBoolean()));
        });
        AtomicInteger stateReads = new AtomicInteger();
        OwnedJourneyReader journeys = new OwnedJourneyReader() {
            @Override public Optional<JourneyLink> findOwned(String owner, UUID journeyId) {
                FixtureJourney row = rows.get(journeyId);
                return row != null && row.owner().equals(owner) ? Optional.of(row.link()) : Optional.empty();
            }
            @Override public Page listOwned(String owner, int limit, String cursor) {
                List<SafeSummary> visible = rows.values().stream().filter(row -> row.owner().equals(owner))
                        .limit(limit).map(row -> new SafeSummary(row.link().journeyId(),
                                states.get(stateKey(row.link().applicationId(), row.link().revision())).status(),
                                actionFor(states.get(stateKey(row.link().applicationId(), row.link().revision())))))
                        .toList();
                return new Page(visible, null);
            }
        };
        ApplicationStateReader stateReader = (applicationId, revision) -> {
            stateReads.incrementAndGet();
            return Optional.ofNullable(states.get(stateKey(applicationId, revision)));
        };
        return new FixturePorts(journeys, stateReader, stateReads);
    }

    private static String stateKey(UUID applicationId, long revision) { return applicationId + "@" + revision; }
    private record FixtureJourney(String owner, JourneyLink link) { }
    private record FixturePorts(OwnedJourneyReader journeys, ApplicationStateReader states, AtomicInteger stateReads) { }

    private static Map<String, Object> referenceResume(String actor, String journeyId, Map<String, JsonNode> byId) {
        JsonNode row = byId.get(journeyId);
        if (actor == null || row == null || !actor.equals(row.path("owner_id").asText()))
            return Map.of("status", 404, "error", "NOT_FOUND");
        String action = row.path("dispatch_confirmed").asBoolean() ? "VIEW_POLICY"
                : row.path("payment_confirmed").asBoolean() ? "WAIT_FOR_DISPATCH" : "CONTINUE_CHECKOUT";
        return Map.of("status", 200, "journeyId", journeyId,
                "businessStatus", row.path("authoritative_status").asText(), "resumeAction", action);
    }

    private static Map<String, Object> unsafeUuidOnlyLookup(String journeyId, Map<String, JsonNode> byId) {
        JsonNode row = byId.get(journeyId);
        return row == null ? Map.of("status", 404, "error", "NOT_FOUND")
                : Map.of("status", 200, "journeyId", journeyId,
                    "businessStatus", row.path("authoritative_status").asText(),
                    "resumeAction", "CONTINUE_CHECKOUT");
    }

    private static boolean validListRequest(String actor, int limit, String cursor) {
        return actor != null && limit >= 1 && limit <= 50
                && (cursor == null || cursor.equals("cursor:" + actor));
    }
}
