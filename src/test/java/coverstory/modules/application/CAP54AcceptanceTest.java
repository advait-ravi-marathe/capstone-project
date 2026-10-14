package coverstory.modules.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.application.api.CAP54Seam;
import coverstory.modules.application.api.CAP54Seam.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP54AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final UUID JOURNEY_A = UUID.fromString("3c7d76dc-29dd-4f2c-a979-8c4a40c8a001");
    private static final UUID APPLICATION_A = UUID.fromString("8b15296a-6a8c-4b71-92d5-0749a760a001");

    @Test void ownerResumeUsesAuthoritativeRevisionAndSafeAction() throws Exception {
        JsonNode contract = caseById("CAP-54-CS-01");
        FixturePorts ports = ports();
        Reply reply = ports.seam.resume(new Principal(contract.path("principal").asText()),
                new ResumeRequest(UUID.fromString(contract.path("journey_id").asText()), null, null));
        assertEquals(contract.path("expected_status").asInt(), reply.status());
        assertEquals(new SafeSummary(JOURNEY_A, "PAYMENT_PENDING", contract.path("expected_action").asText()), reply.journey());
        assertEquals(contract.path("expected_revision").asInt(), ports.revisionSeen.get());
    }

    @Test void foreignAndAbsentJourneyUseSameSafe404BeforeStateRead() throws Exception {
        JsonNode foreignCase = caseById("CAP-54-CS-02a");
        JsonNode absentCase = caseById("CAP-54-CS-02b");
        FixturePorts ports = ports();
        Reply foreign = ports.seam.resume(new Principal(foreignCase.path("principal").asText()),
                new ResumeRequest(UUID.fromString(foreignCase.path("journey_id").asText()), null, null));
        Reply absent = ports.seam.resume(new Principal("user-A"), new ResumeRequest(
                UUID.fromString(absentCase.path("journey_id").asText()), null, null));
        assertEquals(foreignCase.path("expected_status").asInt(), foreign.status());
        assertEquals(foreign, absent);
        assertEquals(foreignCase.path("state_reader_calls").asInt(), ports.stateReads.get());
    }

    @Test void clientOwnerOverrideIsRejectedAndClientStepCannotChangeResumeState() throws Exception {
        JsonNode contract = caseById("CAP-54-CS-03");
        FixturePorts ports = ports();
        Reply override = ports.seam.resume(new Principal("user-A"), new ResumeRequest(JOURNEY_A,
                contract.path("owner_override").asText(), contract.path("browser_current_step").asText()));
        assertEquals(contract.path("expected_status").asInt(), override.status());
        assertEquals(contract.path("repository_calls").asInt(), ports.repositoryReads.get());

        Reply browserStep = ports.seam.resume(new Principal("user-A"), new ResumeRequest(JOURNEY_A, null, "POLICY_ACTIVE"));
        assertEquals("CONTINUE_CHECKOUT", browserStep.journey().resumeAction());
    }

    @Test void anonymousPrivateRequestIsUnauthorized() throws Exception {
        JsonNode contract = caseById("CAP-54-CS-04");
        FixturePorts ports = ports();
        assertEquals(contract.path("private_journey_status").asInt(),
                ports.seam.resume(null, new ResumeRequest(JOURNEY_A, null, null)).status());
    }

    @Test void listBoundariesAndOwnerBoundCursorAreEnforced() throws Exception {
        JsonNode accepted = caseById("CAP-54-CS-05a");
        JsonNode tooLarge = caseById("CAP-54-CS-05b");
        JsonNode wrongOwner = caseById("CAP-54-CS-05c");
        FixturePorts ports = ports();
        assertEquals(200, ports.seam.listOwned(new Principal(accepted.path("principal").asText()),
                accepted.path("limit").asInt(), "cursor:user-A").status());
        assertEquals(tooLarge.path("expected_status").asInt(), ports.seam.listOwned(
                new Principal(tooLarge.path("principal").asText()), tooLarge.path("limit").asInt(), "cursor:user-A").status());
        assertEquals(wrongOwner.path("expected_status").asInt(), ports.seam.listOwned(
                new Principal(wrongOwner.path("principal").asText()), wrongOwner.path("limit").asInt(), "cursor:user-B").status());
    }

    private static JsonNode caseById(String id) throws IOException {
        JsonNode cases = JSON.readTree(Files.readString(Path.of("fixtures/coverstory/CAP-54/synthetic-v1.json"))).path("cases");
        for (JsonNode row : cases) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private static FixturePorts ports() {
        Map<UUID, JourneyLink> links = Map.of(JOURNEY_A,
                new JourneyLink(JOURNEY_A, APPLICATION_A, 2));
        AtomicInteger repositoryReads = new AtomicInteger();
        AtomicInteger stateReads = new AtomicInteger();
        AtomicInteger revisionSeen = new AtomicInteger();
        OwnedJourneyReader reader = new OwnedJourneyReader() {
            @Override public Optional<JourneyLink> findOwned(String owner, UUID journeyId) {
                repositoryReads.incrementAndGet();
                if (!"user-A".equals(owner)) return Optional.empty();
                return Optional.ofNullable(links.get(journeyId));
            }
            @Override public Page listOwned(String owner, int limit, String cursor) {
                repositoryReads.incrementAndGet();
                return new Page(java.util.List.of(), null);
            }
        };
        ApplicationStateReader states = (applicationId, revision) -> {
            stateReads.incrementAndGet(); revisionSeen.set((int) revision);
            return Optional.of(new AuthoritativeState("PAYMENT_PENDING", false, false));
        };
        CursorOwnerVerifier cursors = (cursor, owner) -> cursor == null || cursor.equals("cursor:" + owner);
        return new FixturePorts(new CAP54Seam(reader, states,
                state -> state.dispatchConfirmed() ? "VIEW_POLICY" : state.paymentConfirmed() ? "WAIT_FOR_DISPATCH" : "CONTINUE_CHECKOUT",
                cursors), repositoryReads, stateReads, revisionSeen);
    }

    private record FixturePorts(CAP54Seam seam, AtomicInteger repositoryReads,
                                AtomicInteger stateReads, AtomicInteger revisionSeen) { }
}
