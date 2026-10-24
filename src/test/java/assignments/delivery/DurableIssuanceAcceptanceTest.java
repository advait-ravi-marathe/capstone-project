package assignments.delivery;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Fixed learner acceptance for durable materialization, replay, and transaction boundaries. */
@Tag("learnerAcceptance")
class DurableIssuanceAcceptanceTest {
    @Test
    void day101V003IssuedSeedEventsAreEligibleWithoutStatusWatermark() throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            assertEquals(3, fixture.store.captureCommittedIssuances(20));
            assertEquals(List.of(
                    new IssuanceSourceKey("policy-demo-1", 1),
                    new IssuanceSourceKey("policy-renewal-a", 1),
                    new IssuanceSourceKey("policy-renewal-b", 1)), fixture.workKeys());
            assertTrue(fixture.store.findPending(20).stream().allMatch(e -> e.eventType().equals("ISSUED")));
        }
    }
    @Test
    void day99AcceptsOnlySupportedImmutableIssuedEnvelope() {
        new IssuanceEnvelope("policy-a", 1, "ISSUED", LocalDate.parse("2027-01-26"), 1).validateSupported();
        for (IssuanceEnvelope invalid : List.of(
                new IssuanceEnvelope("", 1, "ISSUED", LocalDate.parse("2027-01-26"), 1),
                new IssuanceEnvelope("policy-a", 0, "ISSUED", LocalDate.parse("2027-01-26"), 1),
                new IssuanceEnvelope("policy-a", 1, "ENDORSED", LocalDate.parse("2027-01-26"), 1),
                new IssuanceEnvelope("policy-a", 1, "ISSUED", null, 1),
                new IssuanceEnvelope("policy-a", 1, "ISSUED", LocalDate.parse("2027-01-26"), 2))) {
            assertThrows(IllegalArgumentException.class, invalid::validateSupported);
        }
    }

    @Test
    void day101RescansForLateEarlierCommitAndOnlyCapturesCommittedIssuedEvents() throws Exception {
        assertLateEarlierCommitIsReconciled();
    }

    @Test
    void day107LateEarlierCommitCannotFallBehindAKeysetWatermark() throws Exception {
        assertLateEarlierCommitIsReconciled();
    }

    private static void assertLateEarlierCommitIsReconciled() throws Exception {
        try (Fixture fixture = new Fixture()) {
            var data = fixture.dataSource;
            try (var late = data.getConnection(); var insert = late.prepareStatement("""
                    INSERT INTO training_policy_lifecycle
                      (policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,
                       annual_premium_rupees,currency,effective_date,status,version)
                    VALUES ('a-late','owner-a','journey-a','quote-a','home-secure-v1','training-rate-v1',
                            125.00,'INR',DATE '2027-01-26','ISSUED',1)
                    """)) {
                late.setAutoCommit(false);
                insert.executeUpdate();
                try (var event = late.prepareStatement("""
                        INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date)
                        VALUES ('a-late',1,'ISSUED',DATE '2027-01-26')
                        """)) { event.executeUpdate(); }
                fixture.insertCommittedSource("b-early", "ISSUED");
                assertEquals(1, fixture.store.captureCommittedIssuances(1));
                assertEquals(List.of(new IssuanceSourceKey("b-early", 1)), fixture.workKeys());
                late.commit();
            }
            assertEquals(1, fixture.store.captureCommittedIssuances(1),
                    "the next reconciliation finds a source key that committed late");
            assertEquals(0, fixture.store.captureCommittedIssuances(1), "completed capture is idempotent");
            assertEquals(List.of(new IssuanceSourceKey("a-late", 1), new IssuanceSourceKey("b-early", 1)), fixture.workKeys());
            fixture.insertCommittedSource("other-event", "ENDORSED");
            fixture.insertCommittedSource("uncommitted-rollback", "ISSUED", true);
            assertEquals(0, fixture.store.captureCommittedIssuances(1));
            assertFalse(fixture.workKeys().contains(new IssuanceSourceKey("other-event", 1)));
            assertFalse(fixture.workKeys().contains(new IssuanceSourceKey("uncommitted-rollback", 1)));
        }
    }

    @Test
    void day101RejectsInvalidCaptureLimitsBeforeChangingRows() throws Exception {
        try (Fixture fixture = new Fixture()) {
            int sourceCountBefore = fixture.committedIssuedSourceCount();
            int workCountBefore = fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_work", Integer.class);
            for (int limit : List.of(0, -1, 21)) {
                assertThrows(IllegalArgumentException.class, () -> fixture.store.captureCommittedIssuances(limit),
                        "capture limit must be in [1,20]: " + limit);
            }
            assertEquals(sourceCountBefore, fixture.committedIssuedSourceCount());
            assertEquals(workCountBefore, fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_work", Integer.class));
        }
    }

    @Test
    void day103ReceiptReplayIsStableAndConflictingPayloadIsVisible() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insertCommittedSource("receipt-a", "ISSUED");
            assertEquals(1, fixture.store.captureCommittedIssuances(1));
            IssuanceEnvelope envelope = fixture.store.findPending(1).getFirst();
            IssuanceEnvelope firstInsertConflict = new IssuanceEnvelope(envelope.policyId(), envelope.sourceVersion(),
                    envelope.eventType(), LocalDate.parse("2027-01-27"), envelope.schemaVersion());
            assertThrows(IllegalStateException.class, () -> fixture.store.acceptReceipt(firstInsertConflict),
                    "the first receipt must match captured work even before a receipt row exists");
            assertEquals(0, fixture.receiptCount(envelope), "first mismatched receipt is rejected without insertion");
            assertEquals(DeliveryReceiptResult.CREATED, fixture.store.acceptReceipt(envelope));
            String originalTimestamp = fixture.receiptTimestamp(envelope);
            assertEquals(DeliveryReceiptResult.IDENTICAL_REPLAY, fixture.store.acceptReceipt(envelope));
            IssuanceEnvelope changed = new IssuanceEnvelope(envelope.policyId(), envelope.sourceVersion(),
                    envelope.eventType(), LocalDate.parse("2027-01-27"), envelope.schemaVersion());
            assertThrows(IllegalStateException.class, () -> fixture.store.acceptReceipt(changed));
            assertEquals(originalTimestamp, fixture.receiptTimestamp(envelope));
            assertEquals(1, fixture.receiptCount(envelope));
        }
    }

    @Test
    void day102MapsCapturedPendingEnvelopeInStableBoundedOrder() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insertCommittedSource("map-a", "ISSUED");
            fixture.insertCommittedSource("map-b", "ISSUED");
            fixture.insertCommittedSource("map-c", "ISSUED");
            assertEquals(3, fixture.store.captureCommittedIssuances(3));
            // Fixture state only: do not invoke Day 104's learner-owned acknowledgment seam.
            fixture.jdbc.update("UPDATE training_issuance_work SET acknowledged_at=TIMESTAMPTZ '2027-01-26 00:00:00+00' WHERE policy_id='map-b' AND source_version=1");
            var first = fixture.store.findPending(1);
            assertEquals(1, first.size());
            assertEquals("map-a", first.getFirst().policyId());
            assertEquals("ISSUED", first.getFirst().eventType());
            assertEquals(LocalDate.parse("2027-01-26"), first.getFirst().effectiveDate());
            assertEquals(List.of("map-a", "map-c"), fixture.store.findPending(10).stream()
                    .map(IssuanceEnvelope::policyId).toList(), "pending reads preserve stable order and exclude the seeded acknowledgment");
        }
    }

    @Test
    void day102RejectsInvalidPendingLimitsWithoutChangingRows() throws Exception {
        try (Fixture fixture = new Fixture()) {
            int workCountBefore = fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_work", Integer.class);
            int receiptCountBefore = fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt", Integer.class);
            for (int limit : List.of(0, -1, 21)) {
                assertThrows(IllegalArgumentException.class, () -> fixture.store.findPending(limit),
                        "pending read limit must be in [1,20]: " + limit);
            }
            assertEquals(workCountBefore, fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_work", Integer.class));
            assertEquals(receiptCountBefore, fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt", Integer.class));
        }
    }

    @Test
    void day102UsesTheCapturedPayloadWhenTheSourceEventRowLaterChanges() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insertCommittedSource("captured-a", "ISSUED");
            assertEquals(1, fixture.store.captureCommittedIssuances(1));
            fixture.jdbc.update("UPDATE training_policy_lifecycle_event SET effective_date=DATE '2030-12-31' WHERE policy_id='captured-a' AND version=1");
            IssuanceEnvelope captured = fixture.store.findPending(1).getFirst();
            assertEquals("captured-a", captured.policyId());
            assertEquals(LocalDate.parse("2027-01-26"), captured.effectiveDate(),
                    "the work row preserves the event payload captured at the boundary");
        }
    }

    @Test
    void day102RejectsAnUnsupportedPersistedEnvelopeSchemaVersion() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insertCommittedSource("malformed-a", "ISSUED");
            assertEquals(1, fixture.store.captureCommittedIssuances(1));
            String constraint = fixture.jdbc.queryForObject("""
                    SELECT conname FROM pg_constraint
                     WHERE conrelid='training_issuance_work'::regclass AND contype='c'
                       AND pg_get_constraintdef(oid) ILIKE '%envelope_schema_version%'
                    """, String.class);
            assertTrue(constraint.matches("[a-zA-Z0-9_]+"), "only a catalog-returned identifier is interpolated");
            fixture.jdbc.execute("ALTER TABLE training_issuance_work DROP CONSTRAINT \"" + constraint + "\"");
            fixture.jdbc.update("UPDATE training_issuance_work SET envelope_schema_version=99 WHERE policy_id='malformed-a' AND source_version=1");
            assertThrows(IllegalArgumentException.class, () -> fixture.store.findPending(1),
                    "unknown persisted envelopes fail visibly instead of being mapped or skipped");
        }
    }

    @Test
    void day104CannotAcknowledgeBeforeCommittedReceiptAndPreservesFirstAcknowledgment() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insertCommittedSource("ack-a", "ISSUED");
            assertEquals(1, fixture.store.captureCommittedIssuances(1));
            IssuanceEnvelope envelope = fixture.store.findPending(1).getFirst();
            assertFalse(fixture.store.acknowledge(envelope), "no receipt means no acknowledgment");
            fixture.store.acceptReceipt(envelope);
            assertTrue(fixture.store.acknowledge(envelope));
            String originalTimestamp = fixture.ackTimestamp(envelope);
            assertFalse(fixture.store.acknowledge(envelope), "repeated acknowledgment changes nothing");
            assertEquals(originalTimestamp, fixture.ackTimestamp(envelope));
        }
    }

    @Test
    void day106ReceiptCommitPrecedesAcknowledgmentAndFailureStopsBatch() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insertCommittedSource("dispatch-a", "ISSUED");
            var events = new ArrayList<String>();
            IssuanceWorkStore observing = new DelegatingStore(fixture.store) {
                @Override public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope envelope) {
                    DeliveryReceiptResult result = super.acceptReceipt(envelope);
                    events.add("receipt-returned");
                    return result;
                }
                @Override public boolean acknowledge(IssuanceEnvelope envelope) {
                    assertEquals(1, fixture.independentReceiptCount(envelope), "receipt must be committed on an independent physical connection first");
                    events.add("acknowledge");
                    return super.acknowledge(envelope);
                }
            };
            OneBatchDispatcher dispatcher = new OneBatchDispatcher(observing, fixture.transactions);
            assertEquals(new BatchDeliveryReport(1, 1, 0, 1, 0), dispatcher.runOnce(10));
            assertEquals(List.of("receipt-returned", "acknowledge"), events);

            fixture.insertCommittedSource("dispatch-fail", "ISSUED");
            IssuanceWorkStore failing = new DelegatingStore(fixture.store) {
                @Override public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope envelope) {
                    throw new IllegalStateException("injected receipt failure");
                }
                @Override public boolean acknowledge(IssuanceEnvelope envelope) {
                    fail("acknowledgment must not follow a failed receipt");
                    return false;
                }
            };
            assertThrows(IllegalStateException.class, () -> new OneBatchDispatcher(failing, fixture.transactions).runOnce(10));
            assertEquals(0, fixture.receiptCount(new IssuanceEnvelope("dispatch-fail", 1, "ISSUED",
                    LocalDate.parse("2027-01-26"), 1)));
        }
    }

    @Test
    void day106RejectsAmbientTransactionAndProcessesAtMostOneBoundedBatch() throws Exception {
        try (Fixture fixture = new Fixture()) {
            for (String id : List.of("bounded-a", "bounded-b", "bounded-c")) fixture.insertCommittedSource(id, "ISSUED");
            var calls = new ArrayList<String>();
            IssuanceWorkStore observing = new DelegatingStore(fixture.store) {
                @Override public int captureCommittedIssuances(int limit) {
                    calls.add("capture:" + limit);
                    return super.captureCommittedIssuances(limit);
                }
                @Override public List<IssuanceEnvelope> findPending(int limit) {
                    calls.add("pending:" + limit);
                    return super.findPending(limit);
                }
            };
            OneBatchDispatcher dispatcher = new OneBatchDispatcher(observing, fixture.transactions);
            var ambient = new org.springframework.transaction.support.TransactionTemplate(fixture.transactions);
            assertThrows(IllegalStateException.class, () -> ambient.execute(status -> dispatcher.runOnce(2)),
                    "the dispatcher cannot silently join an application-owned ambient transaction");
            assertTrue(calls.isEmpty(), "ambient rejection occurs before any capture or delegate call");

            assertEquals(new BatchDeliveryReport(2, 2, 0, 2, 0), dispatcher.runOnce(2));
            assertEquals(List.of("capture:2", "pending:2"), calls,
                    "one invocation captures once and reads one bounded batch; it must not loop over the backlog");
            assertEquals(2, fixture.workKeys().size(), "only the bounded capture batch is materialized");
            assertEquals(3, fixture.committedIssuedSourceCount(), "a source backlog larger than the limit is retained");
            assertEquals(0, fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_work WHERE acknowledged_at IS NULL", Integer.class));
        }
    }

    @Test
    void day106RejectsOutOfRangeBatchLimitBeforeAnyStoreCall() throws Exception {
        try (Fixture fixture = new Fixture()) {
            var calls = new ArrayList<String>();
            IssuanceWorkStore observing = new DelegatingStore(fixture.store) {
                @Override public int captureCommittedIssuances(int limit) {
                    calls.add("capture:" + limit);
                    return super.captureCommittedIssuances(limit);
                }
                @Override public List<IssuanceEnvelope> findPending(int limit) {
                    calls.add("pending:" + limit);
                    return super.findPending(limit);
                }
            };
            OneBatchDispatcher dispatcher = new OneBatchDispatcher(observing, fixture.transactions);
            for (int limit : List.of(0, -1, 21)) {
                assertThrows(IllegalArgumentException.class, () -> dispatcher.runOnce(limit),
                        "dispatch limit must be in [1,20]: " + limit);
            }
            assertTrue(calls.isEmpty(), "invalid limits are rejected before capture or pending reads");
            assertEquals(0, fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_work", Integer.class));
            assertEquals(0, fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt", Integer.class));
        }
    }

    @Test
    void day106ReportsOnlyEarlierCommittedProgressWhenLaterItemFails() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insertCommittedSource("a-first", "ISSUED");
            fixture.insertCommittedSource("z-fail", "ISSUED");
            IssuanceWorkStore failingSecond = new DelegatingStore(fixture.store) {
                @Override public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope envelope) {
                    if (envelope.policyId().equals("z-fail")) throw new IllegalStateException("injected second-item receipt failure");
                    return super.acceptReceipt(envelope);
                }
            };
            BatchDeliveryFailureException failure = assertThrows(BatchDeliveryFailureException.class,
                    () -> new OneBatchDispatcher(failingSecond, fixture.transactions).runOnce(2));
            assertEquals(new BatchDeliveryReport(2, 1, 0, 1, 1), failure.committedProgress());
            assertEquals(1, fixture.receiptCount(new IssuanceEnvelope("a-first", 1, "ISSUED", LocalDate.parse("2027-01-26"), 1)));
            assertEquals(0, fixture.receiptCount(new IssuanceEnvelope("z-fail", 1, "ISSUED", LocalDate.parse("2027-01-26"), 1)));
            assertEquals(1, fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_work WHERE acknowledged_at IS NOT NULL", Integer.class));
            assertEquals(1, fixture.jdbc.queryForObject("SELECT count(*) FROM training_issuance_work WHERE acknowledged_at IS NULL", Integer.class));
        }
    }

    @Test
    void day108ReceiptFailureLeavesPendingWorkForManualRecovery() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insertCommittedSource("failure-a", "ISSUED");
            assertEquals(1, fixture.store.captureCommittedIssuances(1));
            IssuanceEnvelope envelope = fixture.store.findPending(1).getFirst();
            var receiptTx = new org.springframework.transaction.support.TransactionTemplate(fixture.transactions);
            assertThrows(IllegalStateException.class, () -> receiptTx.execute(status -> {
                fixture.store.acceptReceipt(envelope);
                throw new IllegalStateException("injected failure before receipt commit");
            }));
            assertEquals(0, fixture.receiptCount(envelope), "receipt transaction must roll back");
            assertEquals(1, fixture.store.findPending(10).size(), "work stays pending after receipt rollback");
            assertFalse(fixture.store.acknowledge(envelope));
            assertEquals(DeliveryReceiptResult.CREATED, receiptTx.execute(status -> fixture.store.acceptReceipt(envelope)));
            assertTrue(fixture.store.acknowledge(envelope));
            assertEquals(1, fixture.receiptCount(envelope));
        }
    }

    private static class DelegatingStore implements IssuanceWorkStore {
        private final IssuanceWorkStore delegate;
        DelegatingStore(IssuanceWorkStore delegate) { this.delegate = delegate; }
        @Override public int captureCommittedIssuances(int limit) { return delegate.captureCommittedIssuances(limit); }
        @Override public List<IssuanceEnvelope> findPending(int limit) { return delegate.findPending(limit); }
        @Override public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope e) { return delegate.acceptReceipt(e); }
        @Override public boolean acknowledge(IssuanceEnvelope e) { return delegate.acknowledge(e); }
    }

    private static final class Fixture implements AutoCloseable {
        final EmbeddedPostgres postgres;
        final DataSource dataSource;
        final JdbcTemplate jdbc;
        final JdbcIssuanceWorkStore store;
        final DataSourceTransactionManager transactions;
        Fixture() throws Exception { this(true); }
        Fixture(boolean isolateSeedData) throws Exception {
            postgres = EmbeddedPostgres.builder().start();
            dataSource = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
            jdbc = new JdbcTemplate(dataSource);
            if (isolateSeedData) {
                // Test-only cleanup inside this disposable PostgreSQL fixture. A dedicated test above
                // proves V003 ISSUED rows are eligible; production capture never excludes old rows.
                jdbc.update("DELETE FROM training_policy_idempotency");
                jdbc.update("DELETE FROM training_policy_lifecycle_event");
                jdbc.update("DELETE FROM training_policy_lifecycle");
            }
            store = new JdbcIssuanceWorkStore(jdbc);
            transactions = new DataSourceTransactionManager(dataSource);
        }
        void insertCommittedSource(String policyId, String type) { insertCommittedSource(policyId, type, false); }
        void insertCommittedSource(String policyId, String type, boolean rollback) {
            var template = new org.springframework.transaction.support.TransactionTemplate(transactions);
            template.execute(status -> {
                jdbc.update("""
                        INSERT INTO training_policy_lifecycle
                        (policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,
                         annual_premium_rupees,currency,effective_date,status,version)
                        VALUES (?,?,?,?,?,?,125.00,'INR',DATE '2027-01-26','ISSUED',1)
                        """, policyId, "owner-a", "journey-" + policyId, "quote-" + policyId,
                        "home-secure-v1", "training-rate-v1");
                jdbc.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES (?,1,?,DATE '2027-01-26')",
                        policyId, type);
                if (rollback) status.setRollbackOnly();
                return null;
            });
        }
        List<IssuanceSourceKey> workKeys() {
            return jdbc.query("SELECT policy_id,source_version FROM training_issuance_work ORDER BY policy_id,source_version",
                    (rs, n) -> new IssuanceSourceKey(rs.getString(1), rs.getLong(2)));
        }
        int receiptCount(IssuanceEnvelope e) {
            return jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id=? AND source_version=?",
                    Integer.class, e.policyId(), e.sourceVersion());
        }
        int independentReceiptCount(IssuanceEnvelope e) {
            try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(
                    "SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id=? AND source_version=?")) {
                statement.setString(1, e.policyId());
                statement.setLong(2, e.sourceVersion());
                try (var rows = statement.executeQuery()) { rows.next(); return rows.getInt(1); }
            } catch (java.sql.SQLException failure) {
                throw new IllegalStateException("independent receipt observation failed", failure);
            }
        }
        int committedIssuedSourceCount() {
            return jdbc.queryForObject("SELECT count(*) FROM training_policy_lifecycle_event WHERE event_type='ISSUED' AND policy_id LIKE 'bounded-%'", Integer.class);
        }
        String receiptTimestamp(IssuanceEnvelope e) {
            return jdbc.queryForObject("SELECT received_at::text FROM training_issuance_delivery_receipt WHERE policy_id=? AND source_version=?",
                    String.class, e.policyId(), e.sourceVersion());
        }
        String ackTimestamp(IssuanceEnvelope e) {
            return jdbc.queryForObject("SELECT acknowledged_at::text FROM training_issuance_work WHERE policy_id=? AND source_version=?",
                    String.class, e.policyId(), e.sourceVersion());
        }
        @Override public void close() throws Exception { postgres.close(); }
    }
}
