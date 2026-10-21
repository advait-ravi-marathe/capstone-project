package assignments.delivery;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/** JDBC persistence seam over committed ISSUED lifecycle events and local receipts. */
public final class JdbcIssuanceWorkStore implements IssuanceWorkStore {
    private final JdbcTemplate jdbc;

    public JdbcIssuanceWorkStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public int captureCommittedIssuances(int limit) {
        // LEARNER TODO Day 101: rescan missing committed ISSUED events, bounded and idempotently.
        throw todo(101);
    }

    @Override
    public List<IssuanceEnvelope> findPending(int limit) {
        // LEARNER TODO Day 102: map a bounded deterministic list of captured immutable payloads.
        throw todo(102);
    }

    @Override
    public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope envelope) {
        // LEARNER TODO Day 103: commit an immutable receipt; identical replay is a no-op, conflict fails.
        throw todo(103);
    }

    @Override
    public boolean acknowledge(IssuanceEnvelope envelope) {
        // LEARNER TODO Day 104: acknowledge only after receipt commit, preserving original timestamp.
        throw todo(104);
    }

    private static void checkLimit(int limit) {
        if (limit < 1 || limit > 20) throw new IllegalArgumentException("batch limit must be in [1,20]");
    }

    private static UnsupportedOperationException todo(int day) {
        return new UnsupportedOperationException("Day %02d learner TODO: implement issuance work persistence".formatted(day));
    }
}
