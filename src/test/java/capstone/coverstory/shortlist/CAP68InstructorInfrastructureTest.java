package capstone.coverstory.shortlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Disposable SQL reference oracle; this is not a wired shortlist command or API. */
class CAP68InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-68/synthetic-v1.json");
    private static final Path DDL = Path.of("src/test/resources/coverstory/CAP-68/schema.sql");

    @Test void fixtureAndSchemaDeclareSyntheticAtomicShortlistBoundary() throws Exception {
        JsonNode f = fixture();
        assertEquals("SYNTHETIC_POSTGRES_COMMIT_RESPONSE_LOSS_ORACLE", f.path("classification").asText());
        assertFalse(f.path("consumer_data").asBoolean());
        assertFalse(f.path("payment_or_policy_effects").asBoolean());
        assertEquals("UNKNOWN", f.path("expected_client_state_immediately_after_lost_response").asText());
        assertEquals(4, f.path("expected_durable_rows_after_commit").size());
        String ddl = Files.readString(DDL);
        assertTrue(ddl.contains("PRIMARY KEY (journey_id, key_hash)"));
        assertTrue(ddl.contains("UNIQUE (journey_id, operation_id, event_code)"));
        assertTrue(ddl.contains("FOREIGN KEY (journey_id, owner_id)"));
    }

    @Test void normalCommitReturnsCreatedAndPersistsAllAtomicRows() throws Exception {
        try (EmbeddedPostgres pg = freshDatabase()) {
            JsonNode f = fixture();
            seed(pg, f);
            StoredResult result = commit(pg, f);
            assertEquals(f.path("expected_http_status").asInt(), result.status);
            assertEquals(f.path("committed_version").asLong(), result.version);
            assertEquals(uuid(f, "operation_id"), result.operationId);
            assertCommitted(pg, f);
        }
    }

    @Test void responseLossAfterCommitLeavesClientUnknownAndRowsDurable() throws Exception {
        try (EmbeddedPostgres pg = freshDatabase()) {
            JsonNode f = fixture();
            seed(pg, f);
            String clientState = f.path("expected_client_state_immediately_after_lost_response").asText();
            try {
                commit(pg, f);
                throw new LostResponse("synthetic response boundary dropped after PostgreSQL commit");
            } catch (LostResponse expected) {
                assertEquals("UNKNOWN", clientState, "a lost response is not a known transaction failure");
            }
            assertCommitted(pg, f);
        }
    }

    @Test void sameKeyReplayAndOwnerReadRecoverExistingOperationWithoutSecondRow() throws Exception {
        try (EmbeddedPostgres pg = freshDatabase()) {
            JsonNode f = fixture();
            seed(pg, f);
            StoredResult original = commit(pg, f);
            StoredResult replayed = replay(pg, f, bytes(f.path("payload_hash_hex").asText()));
            assertEquals(original, replayed);
            assertEquals(f.path("committed_version").asLong(), ownerGet(pg, uuid(f, "owner_id"), uuid(f, "journey_id")).orElseThrow());
            assertTrue(ownerGet(pg, uuid(f, "other_owner_id"), uuid(f, "journey_id")).isEmpty(),
                    "the UUID lookup identifier is not sufficient without matching owner scope");

            byte[] changedPayload = bytes("3333333333333333333333333333333333333333333333333333333333333333");
            StoredResult conflict = replay(pg, f, changedPayload);
            assertEquals(409, conflict.status, "same key with a different payload conflicts");
            assertCommitted(pg, f);
        }
    }

    @Test void missingOfflineNotificationIsReportedSeparatelyFromCommittedAudit() throws Exception {
        try (EmbeddedPostgres pg = freshDatabase()) {
            JsonNode f = fixture();
            seed(pg, f);
            commit(pg, f);
            FakePublicationPort publication = new FakePublicationPort(false);
            boolean delivered = publication.publish(uuid(f, "operation_id"));
            int shortlistRows = scalar(pg, "SELECT count(*) FROM cap68_harness.shortlist_item");
            int auditRows = scalar(pg, "SELECT count(*) FROM cap68_harness.business_audit");
            GapReport report = new GapReport(shortlistRows, auditRows, delivered, !delivered);
            assertEquals(1, report.shortlistRows);
            assertEquals(1, report.auditRows);
            assertFalse(report.notificationDelivered);
            assertTrue(report.notificationGap, "a SQL/audit receipt does not prove publication delivery");
            assertEquals(0, publication.deliveredCount);
        }
    }

    private static EmbeddedPostgres freshDatabase() throws Exception {
        EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
        try (Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            s.execute(Files.readString(DDL));
        }
        return pg;
    }

    private static void seed(EmbeddedPostgres pg, JsonNode f) throws Exception {
        try (Connection c = pg.getPostgresDatabase().getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO cap68_harness.journey(journey_id, owner_id, version, current_step) VALUES (?, ?, ?, 'SHORTLIST')")) {
            p.setObject(1, uuid(f, "journey_id"));
            p.setObject(2, uuid(f, "owner_id"));
            p.setLong(3, f.path("initial_version").asLong());
            p.executeUpdate();
        }
    }

    /** Instructor reference transaction, deliberately isolated to this test class. */
    private static StoredResult commit(EmbeddedPostgres pg, JsonNode f) throws Exception {
        UUID journey = uuid(f, "journey_id"), owner = uuid(f, "owner_id"), operation = uuid(f, "operation_id");
        long version = f.path("committed_version").asLong();
        try (Connection c = pg.getPostgresDatabase().getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement("INSERT INTO cap68_harness.shortlist_item(item_id,journey_id,owner_id,operation_id,item_code) VALUES (?,?,?,?,?)")) {
                    p.setObject(1, uuid(f, "item_id")); p.setObject(2, journey); p.setObject(3, owner); p.setObject(4, operation);
                    p.setString(5, f.path("synthetic_item_code").asText()); p.executeUpdate();
                }
                execute(c, "INSERT INTO cap68_harness.business_audit VALUES (?,?,?,?,?)", UUID.randomUUID(), journey, owner, operation, "SHORTLIST_ADDED");
                try (PreparedStatement p = c.prepareStatement("UPDATE cap68_harness.journey SET version=?,current_step='SHORTLISTED' WHERE journey_id=? AND owner_id=? AND version=?")) {
                    p.setLong(1, version); p.setObject(2, journey); p.setObject(3, owner); p.setLong(4, f.path("initial_version").asLong());
                    assertEquals(1, p.executeUpdate(), "expected-version update must own the transition");
                }
                execute(c, "INSERT INTO cap68_harness.step_event VALUES (?,?,?,?,?,?)", UUID.randomUUID(), journey, owner, version, operation, "SHORTLIST_ADDED");
                execute(c, "INSERT INTO cap68_harness.command_key VALUES (?,?,?,?,?,?,?)", journey, owner,
                        bytes(f.path("idempotency_key_hash_hex").asText()), bytes(f.path("payload_hash_hex").asText()), operation,
                        f.path("expected_http_status").asInt(), version);
                c.commit();
                return new StoredResult(f.path("expected_http_status").asInt(), version, operation);
            } catch (Exception failure) {
                c.rollback();
                throw failure;
            }
        }
    }

    private static StoredResult replay(EmbeddedPostgres pg, JsonNode f, byte[] payloadHash) throws Exception {
        try (Connection c = pg.getPostgresDatabase().getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT payload_hash,stored_status,stored_version,operation_id FROM cap68_harness.command_key WHERE journey_id=? AND owner_id=? AND key_hash=?")) {
            p.setObject(1, uuid(f, "journey_id")); p.setObject(2, uuid(f, "owner_id")); p.setBytes(3, bytes(f.path("idempotency_key_hash_hex").asText()));
            try (ResultSet r = p.executeQuery()) {
                assertTrue(r.next());
                if (!Arrays.equals(payloadHash, r.getBytes(1))) return new StoredResult(409, 0, uuid(f, "operation_id"));
                return new StoredResult(r.getInt(2), r.getLong(3), r.getObject(4, UUID.class));
            }
        }
    }

    private static Optional<Long> ownerGet(EmbeddedPostgres pg, UUID owner, UUID journey) throws Exception {
        try (Connection c = pg.getPostgresDatabase().getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT version FROM cap68_harness.journey WHERE journey_id=? AND owner_id=?")) {
            p.setObject(1, journey); p.setObject(2, owner);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(r.getLong(1)) : Optional.empty(); }
        }
    }

    private static void assertCommitted(EmbeddedPostgres pg, JsonNode f) throws Exception {
        assertEquals(f.path("committed_version").asLong(), scalar(pg, "SELECT version FROM cap68_harness.journey"));
        JsonNode expected = f.path("expected_durable_rows_after_commit");
        assertEquals(expected.path("shortlist_item").asInt(), scalar(pg, "SELECT count(*) FROM cap68_harness.shortlist_item"));
        assertEquals(expected.path("business_audit").asInt(), scalar(pg, "SELECT count(*) FROM cap68_harness.business_audit"));
        assertEquals(expected.path("step_event").asInt(), scalar(pg, "SELECT count(*) FROM cap68_harness.step_event"));
        assertEquals(expected.path("command_key").asInt(), scalar(pg, "SELECT count(*) FROM cap68_harness.command_key"));
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private static UUID uuid(JsonNode f, String field) { return UUID.fromString(f.path(field).asText()); }
    private static byte[] bytes(String hex) { return java.util.HexFormat.of().parseHex(hex); }
    private static void execute(Connection c, String sql, Object... values) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            for (int i=0;i<values.length;i++) p.setObject(i+1, values[i]);
            p.executeUpdate();
        }
    }
    private static int scalar(EmbeddedPostgres pg, String sql) throws Exception {
        try (Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            assertTrue(r.next()); return r.getInt(1);
        }
    }
    private record StoredResult(int status, long version, UUID operationId) { }
    private record GapReport(int shortlistRows, int auditRows, boolean notificationDelivered, boolean notificationGap) { }
    private static final class FakePublicationPort {
        private final boolean configuredDelivery;
        private int deliveredCount;
        private FakePublicationPort(boolean configuredDelivery) { this.configuredDelivery = configuredDelivery; }
        private boolean publish(UUID operationId) { if (configuredDelivery) deliveredCount++; return configuredDelivery; }
    }
    private static final class LostResponse extends RuntimeException { private LostResponse(String message) { super(message); } }
}
