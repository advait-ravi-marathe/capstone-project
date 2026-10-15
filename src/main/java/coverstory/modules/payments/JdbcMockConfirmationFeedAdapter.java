package coverstory.modules.payments;

import coverstory.modules.application.api.OnboardingPaymentTransitionPort;
import coverstory.modules.payments.api.CAP80Seam;
import coverstory.modules.payments.api.MockConfirmationFeedPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

/** Local mock-only confirmation feed and atomic replay transaction; no insurer/provider client exists. */
public final class JdbcMockConfirmationFeedAdapter implements MockConfirmationFeedPort {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    private final OnboardingPaymentTransitionPort application;

    public JdbcMockConfirmationFeedAdapter(JdbcTemplate jdbc, PlatformTransactionManager transactions,
                                           OnboardingPaymentTransitionPort application) {
        this.jdbc = jdbc; this.transaction = new TransactionTemplate(transactions); this.application = application;
    }

    @Override public Optional<CAP80Seam.OwnerPaymentItem> findOwned(UUID owner, String eventId) {
        return jdbc.query("SELECT owner_id,event_id,sequence,application_id,quote_id,amount_minor,currency,confirmed " +
                        "FROM payments.mock_confirmation_feed WHERE owner_id=? AND event_id=? AND confirmed=true",
                (row, n) -> new CAP80Seam.OwnerPaymentItem(owner, row.getObject("owner_id", UUID.class),
                        row.getString("event_id"), row.getInt("sequence"), row.getObject("application_id", UUID.class),
                        row.getString("quote_id"), row.getLong("amount_minor"), row.getString("currency"), row.getBoolean("confirmed")),
                owner, eventId).stream().findFirst();
    }

    @Override public Optional<CAP80Seam.PaymentCheckpoint> currentCheckpoint(UUID owner) {
        return jdbc.query("SELECT owner_id,last_sequence FROM payments.payment_checkpoint WHERE owner_id=?",
                (row, n) -> new CAP80Seam.PaymentCheckpoint(row.getObject(1, UUID.class), row.getInt(2)), owner).stream().findFirst();
    }

    @Override public CAP80Seam.PaymentEffectPort effectPort() {
        return (owner, work) -> transaction.execute(status -> work.apply(new CAP80Seam.ReplayTransaction() {
            @Override public CAP80Seam.PaymentCheckpoint lockCheckpoint(UUID ownerId) {
                return jdbc.query("SELECT owner_id,last_sequence FROM payments.payment_checkpoint WHERE owner_id=? FOR UPDATE",
                        (row, n) -> new CAP80Seam.PaymentCheckpoint(row.getObject(1, UUID.class), row.getInt(2)), ownerId)
                        .stream().findFirst().orElse(null);
            }
            @Override public void appendMockCapture(CAP80Seam.OwnerPaymentItem item) {
                int inserted = jdbc.update("INSERT INTO payments.mock_capture_effect(event_id,owner_id,sequence,application_id,quote_id,amount_minor,currency) " +
                                "VALUES (?,?,?,?,?,?,?) ON CONFLICT DO NOTHING", item.eventId(), item.feedOwnerId(), item.sequence(),
                        item.applicationId(), item.quoteId(), item.amountMinor(), item.currency());
                if (inserted != 1) throw new IllegalStateException("mock payment effect already exists");
            }
            @Override public void unlockOnboarding(UUID ownerId, CAP80Seam.OwnerPaymentItem item) { application.unlock(ownerId, item); }
            @Override public void advanceCheckpoint(UUID ownerId, int sequence) {
                if (jdbc.update("UPDATE payments.payment_checkpoint SET last_sequence=? WHERE owner_id=?", sequence, ownerId) != 1)
                    throw new IllegalStateException("payment checkpoint unavailable");
            }
        }));
    }
}
