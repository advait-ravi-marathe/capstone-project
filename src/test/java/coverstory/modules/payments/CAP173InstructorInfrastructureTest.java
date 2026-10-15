package coverstory.modules.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.payments.api.PaymentAuditWindowReadPort;
import coverstory.modules.payments.api.PaymentAuditWindowController;
import capstone.coverstory.payments.CAP173AuditAction;
import capstone.coverstory.payments.ReconcilePaymentAuditUtcBoundaries;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/** Independent PostgreSQL + HTTP oracle; the CAP-173 learner seam is not called for positive outcomes. */
class CAP173InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-173/synthetic-v1.json");
    static final UUID OWNER_A = id("17300000-0000-4000-8000-000000000001");
    static final UUID OWNER_B = id("17300000-0000-4000-8000-000000000002");
    private static final Instant START = Instant.parse("2026-12-15T00:00:00Z");
    private static final Instant END = Instant.parse("2026-12-16T00:00:00Z");
    private static final List<UUID> EXPECTED = List.of(id("17300000-0000-4000-8000-000000000102"),
            id("17300000-0000-4000-8000-000000000103"));
    private final ObjectMapper json = new ObjectMapper();

    @Test void fixtureIsSyntheticAndContainsAllBoundariesAndOwnerControl() throws Exception {
        JsonNode root = fixture();
        assertEquals("SYNTHETIC_OFFLINE_PAYMENT_AUDIT_WINDOW", root.path("classification").asText());
        assertFalse(root.path("actual_provider_call").asBoolean());
        assertEquals(5, root.path("records").size());
        assertEquals(2, root.path("acceptance_expectations").path("owner_a_count").asInt());
        assertTrue(root.path("acceptance_expectations").path("owner_b_id_absent_from_a").asBoolean());
    }

    @Test void postgresAdapterUsesOwnerScopeHalfOpenBoundsAndReconciledCount() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            loadSchema(c); seed(c);
            JdbcPaymentAuditWindowAdapter adapter = new JdbcPaymentAuditWindowAdapter(new JdbcTemplate(
                    new SingleConnectionDataSource(c, true)));
            var ids = adapter.selectPaymentAudit(OWNER_A, START, END);
            assertEquals(EXPECTED, ids, "start is inclusive, end is exclusive, and IDs are ordered by event time");
            assertEquals(ids.size(), 2, "reported count reconciles to the exact ID set");
            assertEquals(List.of(id("17300000-0000-4000-8000-000000000105")),
                    adapter.selectPaymentAudit(OWNER_B, START, END));
        }
    }

    @Test void equivalentOffsetAndDatabaseSessionTimezoneDoNotChangeTheWindow() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            loadSchema(c); seed(c);
            try (Connection session = pg.getPostgresDatabase().getConnection();
                 SingleConnectionDataSource ds = new SingleConnectionDataSource(session, true)) {
                JdbcTemplate jdbc = new JdbcTemplate(ds);
                jdbc.execute("SET TIME ZONE 'Asia/Kolkata'");
                JdbcPaymentAuditWindowAdapter adapter = new JdbcPaymentAuditWindowAdapter(jdbc);
                assertEquals(EXPECTED, adapter.selectPaymentAudit(OWNER_A,
                        Instant.parse("2026-12-15T05:30:00+05:30"),
                        Instant.parse("2026-12-16T05:30:00+05:30")));
                assertEquals("Asia/Kolkata", jdbc.queryForObject("SHOW TIME ZONE", String.class));
            }
        }
    }

    @Test void strictEndBoundaryMutantIsDistinguishedByFixture() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            loadSchema(c); seed(c);
            var correct = new JdbcPaymentAuditWindowAdapter(new JdbcTemplate(new SingleConnectionDataSource(c, true)))
                    .selectPaymentAudit(OWNER_A, START, END);
            List<UUID> closedEndMutant;
            try (PreparedStatement ps = c.prepareStatement("""
                    SELECT j.journal_id FROM payments.payment_journal j
                    JOIN payments.payment_intent p ON p.payment_id=j.payment_id
                    WHERE p.owner_id=? AND j.occurred_at>=? AND j.occurred_at<=?
                    ORDER BY j.occurred_at,j.journal_id
                    """)) {
                ps.setObject(1, OWNER_A); ps.setTimestamp(2, java.sql.Timestamp.from(START)); ps.setTimestamp(3, java.sql.Timestamp.from(END));
                try (var rs = ps.executeQuery()) { closedEndMutant = new ArrayList<>(); while (rs.next()) closedEndMutant.add(rs.getObject(1, UUID.class)); }
            }
            assertEquals(EXPECTED, correct);
            assertEquals(3, closedEndMutant.size());
            assertNotEquals(correct, closedEndMutant, "the fixture rejects inclusive-end SQL");
        }
    }

    @Test void referenceHttpPathUsesTestPrincipalAndRejectsAnonymousOrInvalidRangesBeforeSql() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            loadSchema(c); seed(c);
            AtomicInteger reads = new AtomicInteger();
            PaymentAuditWindowReadPort counted = (owner, start, end) -> {
                reads.incrementAndGet();
                return new JdbcPaymentAuditWindowAdapter(new JdbcTemplate(new SingleConnectionDataSource(c, true)))
                        .selectPaymentAudit(owner, start, end);
            };
            CAP173AuditAction reference = new ReferenceAction(counted);
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new PaymentAuditWindowController(reference)).build();
            String start = START.toString(), end = END.toString();
            var anon = mvc.perform(get("/api/v1/me/payments/audit").param("startInclusive", start).param("endExclusive", end)).andReturn();
            assertEquals(401, anon.getResponse().getStatus()); assertEquals(0, reads.get());
            var own = mvc.perform(get("/api/v1/me/payments/audit").principal(OWNER_A::toString)
                    .param("startInclusive", start).param("endExclusive", end)).andReturn();
            assertEquals(200, own.getResponse().getStatus(), own.getResponse().getContentAsString());
            JsonNode ownBody = json.readTree(own.getResponse().getContentAsString());
            assertEquals(2, ownBody.path("count").asInt()); assertEquals(2, ownBody.path("ids").size());
            var invalid = mvc.perform(get("/api/v1/me/payments/audit").principal(OWNER_A::toString)
                    .param("startInclusive", end).param("endExclusive", start)).andReturn();
            assertEquals(400, invalid.getResponse().getStatus()); assertEquals(1, reads.get());
            var otherOwner = mvc.perform(get("/api/v1/me/payments/audit").principal(OWNER_B::toString)
                    .param("startInclusive", start).param("endExclusive", end)).andReturn();
            assertEquals(200, otherOwner.getResponse().getStatus());
            assertEquals(1, json.readTree(otherOwner.getResponse().getContentAsString()).path("count").asInt());
        }
    }

    @Test void actualLearnerSeamRemainsExplicitlyUnimplemented() {
        var learner = new ReconcilePaymentAuditUtcBoundaries((owner, start, end) -> { fail("TODO must not query"); return List.of(); });
        assertThrows(UnsupportedOperationException.class, () -> learner.select(OWNER_A, START, END));
    }

    private final class ReferenceAction implements CAP173AuditAction {
        private final PaymentAuditWindowReadPort port;
        private ReferenceAction(PaymentAuditWindowReadPort port) { this.port = port; }
        @Override public ReconcilePaymentAuditUtcBoundaries.Reply select(UUID owner, Instant start, Instant end) {
            if (owner == null) return new ReconcilePaymentAuditUtcBoundaries.Reply(ReconcilePaymentAuditUtcBoundaries.Status.UNAUTHENTICATED, null);
            if (start == null || end == null || !start.isBefore(end))
                return new ReconcilePaymentAuditUtcBoundaries.Reply(ReconcilePaymentAuditUtcBoundaries.Status.INVALID_RANGE, null);
            List<UUID> ids = List.copyOf(port.selectPaymentAudit(owner, start, end));
            return new ReconcilePaymentAuditUtcBoundaries.Reply(ReconcilePaymentAuditUtcBoundaries.Status.OK,
                    new ReconcilePaymentAuditUtcBoundaries.IdsAndCount(ids, ids.size()));
        }
    }

    private JsonNode fixture() throws Exception { return json.readTree(Files.readString(FIXTURE)); }
    private static UUID id(String raw) { return UUID.fromString(raw); }

    static void loadSchema(Connection c) throws Exception {
        executeResource(c, "/db/schema/ownership.sql");
        executeResource(c, "/db/schema/constraints.sql");
    }

    static void seed(Connection c) throws Exception {
        UUID[] owners = {OWNER_A, OWNER_A, OWNER_A, OWNER_A, OWNER_B};
        Instant[] times = {Instant.parse("2026-12-14T23:59:59.999999Z"), START,
                Instant.parse("2026-12-15T23:59:59.999999Z"), END, Instant.parse("2026-12-15T12:00:00Z")};
        for (int i = 0; i < owners.length; i++) {
            UUID payment = id("17300000-0000-4000-8000-00000000020" + (i + 1));
            UUID journal = id("17300000-0000-4000-8000-00000000010" + (i + 1));
            try (PreparedStatement p = c.prepareStatement("""
                    INSERT INTO payments.payment_intent(payment_id,owner_id,application_id,application_revision,
                      idempotency_key_hash,amount_minor,currency,state)
                    VALUES(?,?,?,1,decode(?,'hex'),10000,'INR','RECORDED')
                    """)) {
                p.setObject(1, payment); p.setObject(2, owners[i]); p.setObject(3, id("17300000-0000-4000-8000-000000000301"));
                p.setString(4, String.format("%064x", i + 1)); p.executeUpdate();
            }
            try (PreparedStatement j = c.prepareStatement("""
                    INSERT INTO payments.payment_journal(journal_id,payment_id,entry_type,amount_minor,currency,occurred_at)
                    VALUES(?,?,'PAYMENT_RECORDED',10000,'INR',?)
                    """)) {
                j.setObject(1, journal); j.setObject(2, payment); j.setTimestamp(3, java.sql.Timestamp.from(times[i])); j.executeUpdate();
            }
        }
    }

    private static void executeResource(Connection c, String path) throws Exception {
        String sql;
        try (var in = CAP173InstructorInfrastructureTest.class.getResourceAsStream(path)) {
            assert in != null : "missing schema " + path;
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Statement s = c.createStatement()) { for (String part : split(sql)) if (!part.isBlank()) s.execute(part); }
    }

    private static List<String> split(String sql) {
        List<String> out = new ArrayList<>(); StringBuilder current = new StringBuilder(); boolean dollar = false;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.startsWith("$$", i)) { dollar = !dollar; current.append("$$"); i++; }
            else if (sql.charAt(i) == ';' && !dollar) { add(out, current.toString()); current.setLength(0); }
            else current.append(sql.charAt(i));
        }
        if (!current.isEmpty()) add(out, current.toString()); return out;
    }

    private static void add(List<String> out, String candidate) {
        String part = candidate.lines().filter(line -> !line.stripLeading().startsWith("--"))
                .reduce((a, b) -> a + "\n" + b).orElse("").trim();
        if (!part.isEmpty()) out.add(part);
    }
}
