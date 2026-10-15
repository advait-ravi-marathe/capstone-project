package coverstory.modules.payments;

import coverstory.modules.payments.api.PaymentAuditWindowReadPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Payments-owned bounded SQL adapter. It reads journal IDs only; it cannot create payment effects. */
public final class JdbcPaymentAuditWindowAdapter implements PaymentAuditWindowReadPort {
    private final JdbcTemplate jdbc;

    public JdbcPaymentAuditWindowAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public List<UUID> selectPaymentAudit(UUID ownerId, Instant startInclusive, Instant endExclusive) {
        if (ownerId == null || startInclusive == null || endExclusive == null || !startInclusive.isBefore(endExclusive))
            throw new IllegalArgumentException("invalid audit window");
        return jdbc.query("""
                SELECT j.journal_id
                  FROM payments.payment_journal j
                  JOIN payments.payment_intent p ON p.payment_id = j.payment_id
                 WHERE p.owner_id = ?
                   AND j.occurred_at >= ?
                   AND j.occurred_at < ?
                 ORDER BY j.occurred_at, j.journal_id
                """, (rs, row) -> rs.getObject(1, UUID.class), ownerId,
                java.sql.Timestamp.from(startInclusive), java.sql.Timestamp.from(endExclusive));
    }
}
