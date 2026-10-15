package coverstory.modules.analytics;

import coverstory.modules.analytics.api.AnalyticsReceiptStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Analytics-owned durable idempotency using a single atomic PostgreSQL batch. */
public final class JdbcAnalyticsReceiptStore implements AnalyticsReceiptStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public JdbcAnalyticsReceiptStore(DataSource dataSource) {
        jdbc = new JdbcTemplate(dataSource);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    @Override public ReceiptResult recordBatch(byte[] ownerHash, List<ReceiptEvent> events) {
        if (ownerHash == null || ownerHash.length != 32 || events == null || events.isEmpty() || events.size() > 10)
            throw new IllegalArgumentException("invalid receipt batch");
        Set<java.util.UUID> ids = new HashSet<>();
        for (ReceiptEvent event : events) {
            if (!ids.add(event.eventId()) || event.payloadHash().length != 32 || event.sessionHash().length != 32)
                throw new IllegalArgumentException("invalid receipt event");
        }
        try {
            return transaction.execute(status -> {
                boolean allDuplicates = true;
                for (ReceiptEvent event : events) {
                    int inserted = jdbc.update("""
                            INSERT INTO analytics.minimal_event_receipt
                              (owner_hash,event_id,payload_hash,session_scope_hash,event_type,page_id,step_code,target_id,duration_ms,server_received_at)
                            VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT (owner_hash,event_id) DO NOTHING
                            """, ownerHash, event.eventId(), event.payloadHash(), event.sessionHash(), event.eventType(),
                            event.pageId(), event.stepCode(), event.targetId(), event.durationMs(),
                            java.sql.Timestamp.from(event.receivedAt()));
                    byte[] prior = jdbc.queryForObject("""
                            SELECT payload_hash FROM analytics.minimal_event_receipt WHERE owner_hash=? AND event_id=?
                            """, byte[].class, ownerHash, event.eventId());
                    if (!MessageDigest.isEqual(prior, event.payloadHash())) throw new EventConflict();
                    if (inserted == 1) allDuplicates = false;
                }
                return new ReceiptResult(false, allDuplicates, events.size());
            });
        } catch (EventConflict conflict) {
            return new ReceiptResult(true, false, 0);
        }
    }

    private static final class EventConflict extends RuntimeException { }
}
