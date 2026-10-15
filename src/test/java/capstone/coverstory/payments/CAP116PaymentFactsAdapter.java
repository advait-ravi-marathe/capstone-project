package capstone.coverstory.payments;

import capstone.coverstory.payments.internal.AcceptedRevisionBalance;
import capstone.coverstory.payments.internal.AcceptedRevisionBalance.*;
import coverstory.modules.payments.api.ConfirmedPaymentEffectFactsPort;
import coverstory.modules.payments.api.PendingAdjustmentFactsPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

/** Test-only read adapter; each query returns observed facts without mutating payment state. */
final class CAP116PaymentFactsAdapter implements ConfirmedPaymentEffectFactsPort, PendingAdjustmentFactsPort {
    private final JdbcTemplate jdbc;
    CAP116PaymentFactsAdapter(javax.sql.DataSource dataSource) { jdbc = new JdbcTemplate(dataSource); }

    @Override public List<ConfirmedEffect> findObservedEffects(UUID applicationId) {
        return jdbc.query("SELECT effect_id,owner_id,application_id,provider_reference,effect_kind,amount_minor,currency,offer_id,revision,content_sha256,authoritative FROM cap116_harness.effect_observation WHERE application_id=? ORDER BY observation_id",
                (rs, row) -> new ConfirmedEffect(rs.getObject("effect_id", UUID.class), rs.getObject("owner_id", UUID.class),
                        rs.getObject("application_id", UUID.class), rs.getString("provider_reference"), EffectKind.valueOf(rs.getString("effect_kind")),
                        rs.getLong("amount_minor"), rs.getString("currency").trim(), rs.getObject("offer_id", UUID.class),
                        rs.getString("revision"), rs.getString("content_sha256"), rs.getBoolean("authoritative")), applicationId);
    }

    @Override public List<PendingAllocation> findPendingAllocations(UUID applicationId) {
        return jdbc.query("SELECT allocation_id,owner_id,application_id,idempotency_key,offer_id,revision,content_sha256,allocation_kind,status,amount_minor,currency FROM cap116_harness.pending_allocation WHERE application_id=? ORDER BY allocation_id",
                (rs, row) -> new PendingAllocation(rs.getObject("allocation_id", UUID.class), rs.getObject("owner_id", UUID.class),
                        rs.getObject("application_id", UUID.class), rs.getString("idempotency_key"), rs.getObject("offer_id", UUID.class),
                        rs.getString("revision"), rs.getString("content_sha256"), AllocationKind.valueOf(rs.getString("allocation_kind")),
                        AllocationStatus.valueOf(rs.getString("status")), rs.getLong("amount_minor"), rs.getString("currency").trim()), applicationId);
    }
}
