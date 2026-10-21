package assignments.delivery.recovery;

import assignments.delivery.IssuanceEnvelope;
import assignments.delivery.IssuanceSourceKey;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** JDBC support for exact-key observation and row-locked recovery decisions. */
public final class JdbcManualRecoveryStore {
    private static final String OBSERVATION_SELECT = """
            SELECT w.policy_id, w.source_version, w.event_type, w.effective_date,
                   w.envelope_schema_version, w.control_revision, w.acknowledged_at,
                   w.quarantined_at, w.quarantine_reason,
                   r.policy_id AS receipt_policy_id, r.event_type AS receipt_event_type,
                   r.effective_date AS receipt_effective_date,
                   r.envelope_schema_version AS receipt_schema_version
              FROM training_issuance_work w
              LEFT JOIN training_issuance_delivery_receipt r
                ON r.policy_id=w.policy_id AND r.source_version=w.source_version
             WHERE w.policy_id=? AND w.source_version=?
            """;

    private final JdbcTemplate jdbc;
    public JdbcManualRecoveryStore(JdbcTemplate jdbc) { this.jdbc = Objects.requireNonNull(jdbc); }

    public RecoveryReadResult inspect(IssuanceSourceKey key) {
        // LEARNER TODO Day 115: inspect the exact key with one joined work/receipt statement,
        // then fetch at most 20 separately-labeled history entries. Propagate DB failures.
        throw new UnsupportedOperationException("Day 115 learner TODO: inspect one exact work identity");
    }

    /** Instructor-supplied lock bridge; lock work first, then observe receipt in a fresh statement. */
    public RecoveryObservation lockAndObserve(IssuanceSourceKey key) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("recovery row lock requires an active transaction");
        List<RecoveryObservation> rows = jdbc.query("""
                SELECT policy_id,source_version,event_type,effective_date,envelope_schema_version,
                       control_revision,acknowledged_at,quarantined_at,quarantine_reason
                  FROM training_issuance_work WHERE policy_id=? AND source_version=? FOR UPDATE
                """, (rs, n) -> mapWorkOnly(key, rs), key.policyId(), key.sourceVersion());
        if (rows.isEmpty()) return RecoveryObservation.notFound(key);
        // Deliberately a new statement after FOR UPDATE may have waited. Do not replace with a pre-lock join.
        ReceiptRelation relation = readReceiptRelation(key, rows.getFirst().envelope());
        RecoveryObservation row = rows.getFirst();
        return new RecoveryObservation(key, true, row.envelope(), row.controlRevision(), row.acknowledgedAt(),
                row.quarantinedAt(), row.quarantineReason(), relation);
    }

    public RecoveryTransitionResult quarantine(IssuanceSourceKey key, int expectedRevision, RecoveryReason reason) {
        // LEARNER TODO Day 116: lock/re-read; validate pending eligibility and revision; atomically
        // write hold + incremented revision + reason-coded history. A conflicting receipt stays visible.
        throw new UnsupportedOperationException("Day 116 learner TODO: quarantine atomically with retained evidence");
    }

    public RecoveryTransitionResult release(IssuanceSourceKey key, int expectedRevision, RecoveryReason reason) {
        // LEARNER TODO Day 117: require the expected active hold/revision; clear only hold metadata,
        // increment revision, and append history in this same transaction. No receipt or ack changes.
        throw new UnsupportedOperationException("Day 117 learner TODO: release only the expected hold revision");
    }

    private RecoveryTransitionResult transition(IssuanceSourceKey key, int expectedRevision, RecoveryAction action, RecoveryReason reason) {
        if (applyControlTransition(key, expectedRevision, action, reason) != 1)
            throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.STALE_REVISION);
        int revision = expectedRevision + 1;
        appendHistory(key, revision, action, reason);
        Instant time = jdbc.queryForObject("SELECT decided_at FROM training_issuance_recovery_history WHERE policy_id=? AND source_version=? AND control_revision=?",
                Timestamp.class, key.policyId(), key.sourceVersion(), revision).toInstant();
        return new RecoveryTransitionResult(key, action, expectedRevision, revision, reason, time);
    }

    /** Fixed, parameterized update helper; callers must already hold the work row lock in a transaction. */
    int applyControlTransition(IssuanceSourceKey key, int expectedRevision, RecoveryAction action,
                               RecoveryReason reason) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("control transition requires an active transaction");
        if (action == RecoveryAction.QUARANTINE) {
            return jdbc.update("""
                    UPDATE training_issuance_work
                       SET quarantined_at=clock_timestamp(), quarantine_reason=?, control_revision=control_revision+1
                     WHERE policy_id=? AND source_version=? AND control_revision=?
                       AND quarantined_at IS NULL AND acknowledged_at IS NULL
                    """, reason.name(), key.policyId(), key.sourceVersion(), expectedRevision);
        }
        return jdbc.update("""
                UPDATE training_issuance_work
                   SET quarantined_at=NULL, quarantine_reason=NULL, control_revision=control_revision+1
                 WHERE policy_id=? AND source_version=? AND control_revision=?
                   AND quarantined_at IS NOT NULL AND acknowledged_at IS NULL
                """, key.policyId(), key.sourceVersion(), expectedRevision);
    }

    void appendHistory(IssuanceSourceKey key, int revision, RecoveryAction action, RecoveryReason reason) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("history append requires an active transaction");
        jdbc.update("""
                INSERT INTO training_issuance_recovery_history
                    (policy_id,source_version,control_revision,action,reason_code)
                VALUES (?,?,?,?,?)
                """, key.policyId(), key.sourceVersion(), revision, action.name(), reason.name());
    }

    private RecoveryObservation mapWorkOnly(IssuanceSourceKey key, ResultSet rs) throws SQLException {
        IssuanceEnvelope envelope = new IssuanceEnvelope(rs.getString("policy_id"), rs.getLong("source_version"),
                rs.getString("event_type"), rs.getDate("effective_date").toLocalDate(), rs.getInt("envelope_schema_version"));
        return new RecoveryObservation(key, true, envelope, rs.getInt("control_revision"),
                instant(rs.getTimestamp("acknowledged_at")), instant(rs.getTimestamp("quarantined_at")),
                reason(rs.getString("quarantine_reason")), ReceiptRelation.ABSENT);
    }

    private RecoveryObservation mapObservation(IssuanceSourceKey key, ResultSet rs) throws SQLException {
        IssuanceEnvelope e = new IssuanceEnvelope(rs.getString("policy_id"), rs.getLong("source_version"), rs.getString("event_type"),
                rs.getDate("effective_date").toLocalDate(), rs.getInt("envelope_schema_version"));
        String type = rs.getString("receipt_event_type");
        ReceiptRelation relation = type == null ? ReceiptRelation.ABSENT : e.eventType().equals(type)
                && e.effectiveDate().equals(rs.getDate("receipt_effective_date").toLocalDate())
                && e.schemaVersion() == rs.getInt("receipt_schema_version") ? ReceiptRelation.MATCHING : ReceiptRelation.CONFLICTING;
        return new RecoveryObservation(key, true, e, rs.getInt("control_revision"), instant(rs.getTimestamp("acknowledged_at")),
                instant(rs.getTimestamp("quarantined_at")), reason(rs.getString("quarantine_reason")), relation);
    }
    private static void requirePresent(RecoveryObservation row) {
        if (!row.workPresent()) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.NOT_FOUND);
    }
    private static void requireRevision(RecoveryObservation row, int expected) {
        if (expected < 0 || row.controlRevision() != expected) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.STALE_REVISION);
    }

    private ReceiptRelation readReceiptRelation(IssuanceSourceKey key, IssuanceEnvelope work) {
        return jdbc.query("""
                SELECT event_type,effective_date,envelope_schema_version
                  FROM training_issuance_delivery_receipt WHERE policy_id=? AND source_version=?
                """, rs -> {
            if (!rs.next()) return ReceiptRelation.ABSENT;
            boolean matches = work.eventType().equals(rs.getString(1))
                    && work.effectiveDate().equals(rs.getDate(2).toLocalDate())
                    && work.schemaVersion() == rs.getInt(3);
            return matches ? ReceiptRelation.MATCHING : ReceiptRelation.CONFLICTING;
        }, key.policyId(), key.sourceVersion());
    }

    private static Instant instant(Timestamp timestamp) { return timestamp == null ? null : timestamp.toInstant(); }
    private static RecoveryReason reason(String value) { return value == null ? null : RecoveryReason.valueOf(value); }
}
