package assignments.delivery.recovery;

import assignments.delivery.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.List;
import java.util.Objects;

/** Shared row-lock guard used by both the original batch and explicit one-key recovery. */
public final class RecoveryAwareIssuanceWorkStore implements IssuanceWorkStore {
    private final IssuanceWorkStore delegate;
    private final JdbcManualRecoveryStore recovery;
    private final JdbcTemplate jdbc;

    public RecoveryAwareIssuanceWorkStore(IssuanceWorkStore delegate, JdbcManualRecoveryStore recovery, JdbcTemplate jdbc) {
        this.delegate = Objects.requireNonNull(delegate); this.recovery = Objects.requireNonNull(recovery); this.jdbc = Objects.requireNonNull(jdbc);
    }
    @Override public int captureCommittedIssuances(int limit) { return delegate.captureCommittedIssuances(limit); }
    @Override public List<IssuanceEnvelope> findPending(int limit) {
        // LEARNER TODO Day 118: validate the bound and filter held work in SQL before ORDER BY/LIMIT.
        throw new UnsupportedOperationException("Day 118 learner TODO: query eligible work before applying the batch limit");
    }
    @Override public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope envelope) {
        requireTransaction();
        RecoveryObservation row = recovery.lockAndObserve(envelope.sourceKey());
        if (!row.workPresent()) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.NOT_FOUND);
        if (row.quarantined()) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.QUARANTINED);
        if (!row.envelope().equals(envelope)) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.UNSUPPORTED_ENVELOPE);
        if (row.receiptRelation() == ReceiptRelation.CONFLICTING) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.RECEIPT_CONFLICT);
        return delegate.acceptReceipt(envelope);
    }
    @Override public boolean acknowledge(IssuanceEnvelope envelope) {
        requireTransaction();
        RecoveryObservation row = recovery.lockAndObserve(envelope.sourceKey());
        if (!row.workPresent()) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.NOT_FOUND);
        if (row.quarantined()) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.QUARANTINED);
        if (!row.envelope().equals(envelope)) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.UNSUPPORTED_ENVELOPE);
        if (row.receiptRelation() != ReceiptRelation.MATCHING)
            throw new RecoveryPreconditionException(row.receiptRelation() == ReceiptRelation.CONFLICTING
                    ? RecoveryPreconditionException.Code.RECEIPT_CONFLICT : RecoveryPreconditionException.Code.ACK_WITHOUT_RECEIPT);
        return delegate.acknowledge(envelope);
    }
    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("recovery-aware write requires transaction");
    }
}
