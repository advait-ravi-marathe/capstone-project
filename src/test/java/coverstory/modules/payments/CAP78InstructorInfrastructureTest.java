package coverstory.modules.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Actual PostgreSQL schema and transaction oracle; CAP78Seam remains a learner TODO. */
class CAP78InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-78/synthetic-v1.json");
    private static final Path SOURCE_FIXTURE = Path.of("src/test/resources/coverstory/payments/cap_78_fixtures.sql");
    private static final UUID OWNER_A = id("78000000-0000-4000-8000-000000000001");
    private static final UUID OWNER_B = id("78000000-0000-4000-8000-000000000002");
    private static final UUID JOURNEY_A = id("78000000-0000-4000-8000-000000000101");
    private static final UUID APP_A = id("78000000-0000-4000-8000-000000000111");
    private static final UUID QUOTE_A = id("78000000-0000-4000-8000-000000000201");
    private static final UUID VALIDATION_A = id("78000000-0000-4000-8000-000000000401");
    private static final long AMOUNT = 100_000L;
    private static final String TERMS = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String KEY = "cap78-synthetic-idempotency-key-A";
    private static final String REQUEST = "req-cap78-synth-001";
    private static final Instant NOW = Instant.parse("2026-11-20T10:01:00Z");
    private static final String MARKER = "CAP78-SYNTHETIC-PRIVATE-MARKER-DO-NOT-PERSIST";

    @Test void fixtureAndPostgresConstraintsLoadOnlySyntheticApprovedQuote() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("actual_provider_call").asBoolean());
        assertEquals(9, root.path("cases").size());
        assertEquals(MARKER, root.path("protected_payload_canary").asText());
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            load(c);
            assertEquals(1, scalarInt(s, "SELECT count(*) FROM application.journey WHERE journey_id='" + JOURNEY_A + "'"));
            assertEquals(100000L, scalarLong(s, "SELECT total_minor FROM catalog.approved_quote WHERE quote_id='" + QUOTE_A + "'"));
            assertThrows(SQLException.class, () -> s.executeUpdate("INSERT INTO payments.mock_order(order_id,owner_id,journey_id,application_id,quote_id,quote_revision,amount_minor,currency,terms_hash,idempotency_key_hash,request_hash,operation_id,state) VALUES ('78000000-0000-4000-8000-000000000901','" + OWNER_A + "','" + JOURNEY_A + "','" + APP_A + "','" + QUOTE_A + "',2,0,'INR','" + TERMS + "',decode(repeat('11',32),'hex'),decode(repeat('22',32),'hex'),'78000000-0000-4000-8000-000000000902','PENDING')"));
        }
    }

    @Test void successfulIntentAndLostResponseRetryCommitOneAuthoritativeTransition() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            load(c);
            AtomicInteger analyticsCalls = new AtomicInteger();
            Result first = referenceCheckout(c, new Input(OWNER_A, JOURNEY_A, 4, VALIDATION_A, AMOUNT, TERMS, KEY), 0,
                    () -> { analyticsCalls.incrementAndGet(); throw new IllegalStateException("telemetry unavailable"); });
            assertEquals(201, first.status()); assertEquals("CREATED", first.code()); assertFalse(first.replay());
            assertEquals(5, first.journeyVersion()); assertNotNull(first.orderId()); assertNotNull(first.attemptId());
            assertCounts(c, 1, 1, 1, 1, 1);
            assertEquals("PENDING", scalar(c, "SELECT state FROM payments.payment_attempt"));
            assertEquals("AWAITING_PAYMENT", scalar(c, "SELECT status FROM application.application_record"));
            assertEquals("PAYMENT_PENDING", scalar(c, "SELECT current_step FROM application.journey"));
            assertEquals(5, scalarInt(c, "SELECT revision FROM application.journey"));
            assertEquals("WAITING", scalar(c, "SELECT status FROM application.journey"));
            assertEquals("MOCK_PAYMENT_PENDING", scalar(c, "SELECT event_code FROM business_audit.transaction_audit_event"));
            assertEquals(0, scalarInt(c, "SELECT count(*) FROM policy.dispatch"));
            assertEquals(1, analyticsCalls.get(), "analytics is called after commit and cannot fail checkout");

            Result replay = referenceCheckout(c, new Input(OWNER_A, JOURNEY_A, 4, VALIDATION_A, AMOUNT, TERMS, KEY), 0, () -> { });
            assertEquals(200, replay.status()); assertEquals("REPLAYED", replay.code()); assertTrue(replay.replay());
            assertEquals(first.orderId(), replay.orderId()); assertEquals(first.attemptId(), replay.attemptId());
            assertEquals(first.operationId(), replay.operationId()); assertEquals(5, replay.journeyVersion());
            assertCounts(c, 1, 1, 1, 1, 1);
            // Homepage state is a fresh authoritative projection, independent of browser events.
            assertEquals("PAYMENT_PENDING", scalar(c, "SELECT current_step FROM application.journey WHERE owner_id='" + OWNER_A + "'"));
            assertEquals(0, scalarInt(c, "SELECT count(*) FROM analytics.behavior_event"));
        }
    }

    @Test void everyInjectedFailureBetweenDomainOutboxAuditAndJourneyWritesRollsBack() throws Exception {
        for (int failAfter = 1; failAfter <= 7; failAfter++) {
            try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
                load(c);
                final int point = failAfter;
                SQLException error = assertThrows(SQLException.class, () -> referenceCheckout(c,
                        new Input(OWNER_A, JOURNEY_A, 4, VALIDATION_A, AMOUNT, TERMS, KEY), point, () -> { }));
                assertTrue(error.getMessage().contains("injected CAP78 failure"), "failure point " + point);
                assertCounts(c, 0, 0, 0, 0, 0);
                assertEquals("DRAFT", scalar(c, "SELECT status FROM application.application_record"));
                assertEquals("QUOTE_REVIEW", scalar(c, "SELECT current_step FROM application.journey"));
                assertEquals(4, scalarInt(c, "SELECT revision FROM application.journey"));
            }
        }
    }

    @Test void sameKeyPayloadConflictWithdrawnSourceAndStaleJourneyCreateNoNewIntent() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            load(c);
            Result stale = referenceCheckout(c, new Input(OWNER_A, JOURNEY_A, 3, VALIDATION_A, AMOUNT, TERMS, "key-stale"), 0, () -> { });
            assertEquals(409, stale.status()); assertEquals("STALE_JOURNEY", stale.code());
            Result first = referenceCheckout(c, new Input(OWNER_A, JOURNEY_A, 4, VALIDATION_A, AMOUNT, TERMS, KEY), 0, () -> { });
            assertEquals(201, first.status());
            Result conflict = referenceCheckout(c, new Input(OWNER_A, JOURNEY_A, 4, VALIDATION_A, AMOUNT + 1, TERMS, KEY), 0, () -> { });
            assertEquals(409, conflict.status()); assertEquals("IDEMPOTENCY_CONFLICT", conflict.code());
            Result termsConflict = referenceCheckout(c, new Input(OWNER_A, JOURNEY_A, 4, VALIDATION_A, AMOUNT,
                    "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc", KEY), 0, () -> { });
            assertEquals(409, termsConflict.status()); assertEquals("IDEMPOTENCY_CONFLICT", termsConflict.code());
            assertCounts(c, 1, 1, 1, 1, 1);
            assertEquals("AWAITING_PAYMENT", scalar(c, "SELECT status FROM application.application_record"));
        }
    }

    @Test void withdrawnSourceIllustrationRejectsBeforeAnyPurchaseWrite() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            load(c);
            try (Statement s = c.createStatement()) {
                s.executeUpdate("UPDATE catalog.approved_quote SET source_state='WITHDRAWN' WHERE quote_id='" + QUOTE_A + "'");
            }
            Result rejected = referenceCheckout(c, new Input(OWNER_A, JOURNEY_A, 4, VALIDATION_A, AMOUNT, TERMS, "key-withdrawn"), 0, () -> { });
            assertEquals(422, rejected.status()); assertEquals("REVIEW_REQUIRED", rejected.code());
            assertCounts(c, 0, 0, 0, 0, 0);
            assertEquals("DRAFT", scalar(c, "SELECT status FROM application.application_record"));
        }
    }

    @Test void foreignOwnerGetsSameSafeNotFoundBeforeSensitivePortsAndMarkerNeverPersists() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            load(c);
            Counters counters = new Counters();
            Result foreign = referenceCheckout(c, new Input(OWNER_B, JOURNEY_A, 4, VALIDATION_A, AMOUNT, TERMS, KEY), 0, () -> { }, counters);
            assertEquals(404, foreign.status()); assertEquals("NOT_FOUND", foreign.code());
            assertEquals(0, counters.validationReads); assertEquals(0, counters.decryptCalls); assertEquals(0, counters.providerCalls);
            assertCounts(c, 0, 0, 0, 0, 0);
            assertMarkerAbsentFromAllTextColumns(c);
            assertEquals(0, scalarInt(c, "SELECT count(*) FROM information_schema.columns WHERE table_schema IN ('application','payments','business_audit','analytics') AND column_name ILIKE '%url%'"));
            assertFalse(foreign.toString().contains(MARKER));
            assertFalse(Files.readString(SOURCE_FIXTURE).contains(MARKER));
        }
    }

    private record Input(UUID owner, UUID journey, long version, UUID validation, long amount,
                         String terms, String key) { }
    private record Result(int status, String code, UUID orderId, UUID attemptId, UUID operationId,
                          long journeyVersion, boolean replay) { }
    private static final class Counters { int validationReads; int decryptCalls; int providerCalls; }

    /** Independent test oracle only; SQL writes below are always joined by one JDBC transaction. */
    private static Result referenceCheckout(Connection c, Input in, int failAfter, Runnable analytics) throws Exception {
        return referenceCheckout(c, in, failAfter, analytics, new Counters());
    }

    private static Result referenceCheckout(Connection c, Input in, int failAfter, Runnable analytics,
                                             Counters counters) throws Exception {
        byte[] keyHash = sha256(in.key().getBytes(StandardCharsets.UTF_8));
        byte[] requestHash = sha256((in.owner() + "|" + in.journey() + "|" + in.version() + "|" + in.validation()
                + "|" + in.amount() + "|" + in.terms()).getBytes(StandardCharsets.UTF_8));
        c.setAutoCommit(false);
        int[] writes = {0};
        Result result;
        try {
            try (PreparedStatement ps = c.prepareStatement("SELECT o.order_id,o.operation_id,o.request_hash,a.attempt_id,j.revision FROM payments.mock_order o JOIN payments.payment_attempt a USING(order_id) JOIN application.journey j ON j.journey_id=o.journey_id WHERE o.owner_id=? AND o.idempotency_key_hash=? FOR UPDATE")) {
                ps.setObject(1, in.owner()); ps.setBytes(2, keyHash);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (!MessageDigest.isEqual(requestHash, rs.getBytes("request_hash"))) {
                            c.rollback(); return new Result(409, "IDEMPOTENCY_CONFLICT", null, null, null, 0, false);
                        }
                        result = new Result(200, "REPLAYED", rs.getObject("order_id", UUID.class),
                                rs.getObject("attempt_id", UUID.class), rs.getObject("operation_id", UUID.class),
                                rs.getLong("revision"), true);
                        c.commit();
                        safeAnalytics(analytics, in.journey());
                        return result;
                    }
                }
            }

            UUID appId; UUID journeyQuote; UUID appQuote; long journeyVersion; long appRevision; String currentStep; String appStatus;
            try (PreparedStatement ps = c.prepareStatement("SELECT j.application_id,j.quote_id,j.revision,j.current_step,a.quote_id AS app_quote,a.revision AS app_revision,a.status AS app_status FROM application.journey j JOIN application.application_record a ON a.application_id=j.application_id AND a.owner_id=j.owner_id WHERE j.journey_id=? AND j.owner_id=? FOR UPDATE OF j,a")) {
                ps.setObject(1, in.journey()); ps.setObject(2, in.owner());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return new Result(404, "NOT_FOUND", null, null, null, 0, false); }
                    appId = rs.getObject("application_id", UUID.class); journeyQuote = rs.getObject("quote_id", UUID.class);
                    appQuote = rs.getObject("app_quote", UUID.class);
                    journeyVersion = rs.getLong("revision"); currentStep = rs.getString("current_step");
                    appRevision = rs.getLong("app_revision"); appStatus = rs.getString("app_status");
                }
            }
            if (journeyVersion != in.version() || !"QUOTE_REVIEW".equals(currentStep)) {
                c.rollback(); return new Result(409, "STALE_JOURNEY", null, null, null, journeyVersion, false);
            }

            counters.validationReads++;
            UUID quoteId; long quoteRevision; long sourceQuoteRevision; long serverAmount; String currency; String serverTerms;
            String sourceState; Instant expires; boolean accepted;
            try (PreparedStatement ps = c.prepareStatement("SELECT v.quote_id,v.quote_revision,v.accepted_total_minor,v.terms_hash,v.expires_at,v.accepted,q.quote_revision AS source_quote_revision,q.total_minor,q.currency,q.terms_hash AS quote_terms,q.source_state FROM application.quote_validation v JOIN catalog.approved_quote q USING(quote_id,owner_id) WHERE v.validation_id=? AND v.owner_id=? AND v.journey_id=? AND v.quote_id=? FOR UPDATE OF v,q")) {
                ps.setObject(1, in.validation()); ps.setObject(2, in.owner()); ps.setObject(3, in.journey()); ps.setObject(4, journeyQuote);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return new Result(404, "NOT_FOUND", null, null, null, journeyVersion, false); }
                    quoteId = rs.getObject("quote_id", UUID.class); quoteRevision = rs.getLong("quote_revision");
                    sourceQuoteRevision = rs.getLong("source_quote_revision");
                    serverAmount = rs.getLong("total_minor"); currency = rs.getString("currency");
                    serverTerms = rs.getString("quote_terms"); expires = rs.getTimestamp("expires_at").toInstant();
                    accepted = rs.getBoolean("accepted") && rs.getLong("accepted_total_minor") == in.amount()
                            && rs.getString("terms_hash").equals(in.terms()) && rs.getString("terms_hash").equals(serverTerms);
                    sourceState = rs.getString("source_state");
                }
            }
            if (!accepted || !"ACTIVE".equals(sourceState) || !expires.isAfter(NOW)
                    || serverAmount != in.amount() || quoteRevision != sourceQuoteRevision
                    || !journeyQuote.equals(appQuote)
                    || !journeyQuote.equals(quoteId) || !"DRAFT".equals(appStatus) || !"INR".equals(currency)) {
                c.rollback(); return new Result(422, "REVIEW_REQUIRED", null, null, null, journeyVersion, false);
            }

            UUID order = UUID.randomUUID(), attempt = UUID.randomUUID(), operation = UUID.randomUUID();
            UUID outbox = UUID.randomUUID(), audit = UUID.randomUUID(), stepEvent = UUID.randomUUID();
            String requestId = REQUEST;
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO payments.mock_order(order_id,owner_id,journey_id,application_id,quote_id,quote_revision,amount_minor,currency,terms_hash,idempotency_key_hash,request_hash,operation_id,state) VALUES (?,?,?,?,?,?,?,?,?,?,?,?, 'PENDING')")) {
                ps.setObject(1, order); ps.setObject(2, in.owner()); ps.setObject(3, in.journey()); ps.setObject(4, appId);
                ps.setObject(5, quoteId); ps.setLong(6, quoteRevision); ps.setLong(7, serverAmount); ps.setString(8, currency.trim());
                ps.setString(9, serverTerms); ps.setBytes(10, keyHash); ps.setBytes(11, requestHash); ps.setObject(12, operation);
                ps.executeUpdate();
            }
            afterWrite(writes, failAfter);
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO payments.payment_attempt(attempt_id,order_id,owner_id,state,amount_minor,currency) VALUES (?,?,?,'PENDING',?,'INR')")) {
                ps.setObject(1, attempt); ps.setObject(2, order); ps.setObject(3, in.owner()); ps.setLong(4, serverAmount); ps.executeUpdate();
            }
            afterWrite(writes, failAfter);
            try (PreparedStatement ps = c.prepareStatement("UPDATE application.application_record SET status='AWAITING_PAYMENT',updated_at=? WHERE application_id=? AND owner_id=? AND revision=? AND status='DRAFT'")) {
                ps.setTimestamp(1, Timestamp.from(NOW)); ps.setObject(2, appId); ps.setObject(3, in.owner()); ps.setLong(4, appRevision);
                if (ps.executeUpdate() != 1) throw new SQLException("stale application revision");
            }
            afterWrite(writes, failAfter);
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO payments.outbox_intent(outbox_id,operation_id,attempt_id,event_type,status,request_id) VALUES (?,?,?,'MOCK_PAYMENT_PENDING','PENDING',?)")) {
                ps.setObject(1, outbox); ps.setObject(2, operation); ps.setObject(3, attempt); ps.setString(4, requestId); ps.executeUpdate();
            }
            afterWrite(writes, failAfter);
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO business_audit.transaction_audit_event(event_id,owner_id,aggregate_type,aggregate_id,aggregate_sequence,journey_id,operation_reference,event_code,prior_state,new_state,amount_minor,currency,request_id,occurred_at) VALUES (?,?,'PAYMENT',?,1,?,?,'MOCK_PAYMENT_PENDING','NONE','PENDING',?,'INR',?,?)")) {
                ps.setObject(1, audit); ps.setObject(2, in.owner()); ps.setObject(3, attempt); ps.setObject(4, in.journey());
                ps.setObject(5, operation); ps.setLong(6, serverAmount); ps.setString(7, requestId); ps.setTimestamp(8, Timestamp.from(NOW)); ps.executeUpdate();
            }
            afterWrite(writes, failAfter);
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO application.journey_step_event(step_event_id,journey_id,sequence,event_code,from_step,to_step,actor_ref,domain_record_type,domain_record_id,domain_revision,causation_id,request_id,occurred_at) VALUES (?,?,?,'MOCK_PAYMENT_PENDING','QUOTE_REVIEW','PAYMENT_PENDING',?,'PAYMENT_ATTEMPT',?,? ,?,?,?)")) {
                ps.setObject(1, stepEvent); ps.setObject(2, in.journey()); ps.setLong(3, journeyVersion + 1); ps.setObject(4, in.owner());
                ps.setObject(5, attempt); ps.setLong(6, appRevision); ps.setObject(7, operation); ps.setString(8, requestId); ps.setTimestamp(9, Timestamp.from(NOW)); ps.executeUpdate();
            }
            afterWrite(writes, failAfter);
            try (PreparedStatement ps = c.prepareStatement("UPDATE application.journey SET revision=revision+1,current_step='PAYMENT_PENDING',status='WAITING',updated_at=? WHERE journey_id=? AND owner_id=? AND revision=?")) {
                ps.setTimestamp(1, Timestamp.from(NOW)); ps.setObject(2, in.journey()); ps.setObject(3, in.owner()); ps.setLong(4, journeyVersion);
                if (ps.executeUpdate() != 1) throw new SQLException("stale journey revision");
            }
            afterWrite(writes, failAfter);
            c.commit();
            result = new Result(201, "CREATED", order, attempt, operation, journeyVersion + 1, false);
        } catch (Exception e) {
            c.rollback();
            if (e instanceof SQLException sql) throw sql;
            throw new SQLException(e);
        } finally {
            c.setAutoCommit(true);
        }
        safeAnalytics(analytics, in.journey());
        return result;
    }

    private static void safeAnalytics(Runnable analytics, UUID journeyId) {
        try { analytics.run(); } catch (RuntimeException ignored) { /* best effort after commit */ }
    }

    private static void afterWrite(int[] writes, int failAfter) throws SQLException {
        writes[0]++;
        if (writes[0] == failAfter) throw new SQLException("injected CAP78 failure after write " + writes[0]);
    }

    private static void load(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            executeResource(s, "/db/schema/ownership.sql");
            executeResource(s, "/db/schema/constraints.sql");
            executeResource(s, "/db/schema/cap78_purchase.sql");
            executeSql(s, Files.readString(SOURCE_FIXTURE));
        }
    }

    private static void executeResource(Statement s, String resource) throws Exception {
        try (var in = CAP78InstructorInfrastructureTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, "missing SQL resource " + resource);
            executeSql(s, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static void executeSql(Statement s, String sql) throws Exception {
        boolean single = false, dollar = false, lineComment = false; StringBuilder statement = new StringBuilder();
        for (int i = 0; i < sql.length(); i++) {
            if (!single && !dollar && sql.startsWith("--", i)) lineComment = true;
            if (lineComment) { char c = sql.charAt(i); if (c == '\n') { lineComment = false; statement.append(c); } continue; }
            if (!single && sql.startsWith("$$", i)) { dollar = !dollar; statement.append("$$"); i++; continue; }
            char ch = sql.charAt(i);
            if (!dollar && ch == '\'') single = !single;
            if (!single && !dollar && ch == ';') {
                if (!statement.toString().isBlank()) s.execute(statement.toString());
                statement.setLength(0);
            } else statement.append(ch);
        }
        if (!statement.toString().isBlank()) s.execute(statement.toString());
    }

    private static void assertCounts(Connection c, int orders, int attempts, int outbox,
                                     int audits, int steps) throws Exception {
        assertEquals(orders, scalarInt(c, "SELECT count(*) FROM payments.mock_order"));
        assertEquals(attempts, scalarInt(c, "SELECT count(*) FROM payments.payment_attempt"));
        assertEquals(outbox, scalarInt(c, "SELECT count(*) FROM payments.outbox_intent"));
        assertEquals(audits, scalarInt(c, "SELECT count(*) FROM business_audit.transaction_audit_event"));
        assertEquals(steps, scalarInt(c, "SELECT count(*) FROM application.journey_step_event"));
    }

    private static void assertMarkerAbsentFromAllTextColumns(Connection c) throws Exception {
        String columns = "SELECT table_schema,table_name,column_name FROM information_schema.columns "
                + "WHERE table_schema IN ('application','payments','business_audit','analytics','catalog') "
                + "AND data_type IN ('text','character varying','character')";
        List<String[]> names = new ArrayList<>();
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(columns)) {
            while (rs.next()) names.add(new String[] {rs.getString(1), rs.getString(2), rs.getString(3)});
        }
        for (String[] name : names) {
            String table = "\"" + name[0] + "\".\"" + name[1] + "\"";
            String column = "\"" + name[2] + "\"";
            try (PreparedStatement query = c.prepareStatement("SELECT count(*) FROM " + table
                    + " WHERE CAST(" + column + " AS TEXT) LIKE ?")) {
                query.setString(1, "%" + MARKER + "%");
                try (ResultSet found = query.executeQuery()) { assertTrue(found.next()); assertEquals(0, found.getInt(1), table + "." + column); }
            }
        }
    }

    private static String scalar(Connection c, String query) throws Exception {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(query)) { assertTrue(rs.next()); return rs.getString(1); }
    }
    private static int scalarInt(Statement s, String query) throws Exception {
        try (ResultSet rs = s.executeQuery(query)) { assertTrue(rs.next()); return rs.getInt(1); }
    }
    private static int scalarInt(Connection c, String query) throws Exception {
        try (Statement s = c.createStatement()) { return scalarInt(s, query); }
    }
    private static long scalarLong(Statement s, String query) throws Exception {
        try (ResultSet rs = s.executeQuery(query)) { assertTrue(rs.next()); return rs.getLong(1); }
    }
    private static byte[] sha256(byte[] value) throws Exception { return MessageDigest.getInstance("SHA-256").digest(value); }
    private static UUID id(String value) { return UUID.fromString(value); }
}
