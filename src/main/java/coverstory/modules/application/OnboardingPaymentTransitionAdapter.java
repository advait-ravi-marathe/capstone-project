package coverstory.modules.application;

import coverstory.modules.application.api.OnboardingPaymentTransitionPort;
import coverstory.modules.payments.api.CAP80Seam.OwnerPaymentItem;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

/** Application-owned SQL adapter. It participates in the caller's datasource transaction. */
public final class OnboardingPaymentTransitionAdapter implements OnboardingPaymentTransitionPort {
    private final JdbcTemplate jdbc;
    public OnboardingPaymentTransitionAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public void unlock(UUID owner, OwnerPaymentItem item) {
        int changed = jdbc.update("UPDATE application.onboarding_case SET state='ONBOARDING', payment_event_id=? " +
                        "WHERE application_id=? AND owner_id=? AND quote_id=? AND state='AWAITING_PAYMENT'",
                item.eventId(), item.applicationId(), owner, item.quoteId());
        if (changed != 1) throw new IllegalStateException("owned onboarding transition unavailable");
        jdbc.update("INSERT INTO application.onboarding_timeline(timeline_id,application_id,owner_id,payment_event_id,from_state,to_state) " +
                "VALUES (?,?,?,?,'AWAITING_PAYMENT','ONBOARDING')", UUID.randomUUID(), item.applicationId(), owner, item.eventId());
    }
}
