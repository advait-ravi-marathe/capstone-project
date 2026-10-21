package assignments.delivery.recovery;

import assignments.delivery.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Objects;

/** Explicit single-identity operations. Receipt and acknowledgment are always separate commits. */
public final class ManualRecoveryService {
    private final JdbcManualRecoveryStore store;
    private final IssuanceWorkStore guardedStore;
    private final TransactionTemplate read, control, receipt, acknowledgment;
    public ManualRecoveryService(JdbcManualRecoveryStore store, IssuanceWorkStore guardedStore, PlatformTransactionManager manager) {
        this.store=Objects.requireNonNull(store); this.guardedStore=Objects.requireNonNull(guardedStore);
        read=template(manager,true); control=template(manager,false); receipt=template(manager,false); acknowledgment=template(manager,false);
        read.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        control.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        receipt.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        acknowledgment.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }
    private static TransactionTemplate template(PlatformTransactionManager manager, boolean ro) {
        TransactionTemplate t=new TransactionTemplate(manager); t.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); t.setReadOnly(ro); return t;
    }
    public RecoveryReadResult inspect(IssuanceSourceKey key) { return read.execute(s -> store.inspect(key)); }
    public RecoveryTransitionResult quarantine(IssuanceSourceKey key,int revision,RecoveryReason reason) {
        return control.execute(s -> store.quarantine(key,revision,reason));
    }
    public RecoveryTransitionResult release(IssuanceSourceKey key,int revision,RecoveryReason reason) {
        return control.execute(s -> store.release(key,revision,reason));
    }
    public String retryOne(IssuanceSourceKey key,int expectedRevision) {
        // LEARNER TODO Day 120: retry exactly one stable key in separate receipt and ack transactions;
        // reject an ambient transaction before any observation or store call.
        throw new UnsupportedOperationException("Day 120 learner TODO: recover one explicit work identity");
    }

    private void assertStillEligible(IssuanceSourceKey key, int expectedRevision) {
        RecoveryObservation current=store.lockAndObserve(key);
        if (!current.workPresent()) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.NOT_FOUND);
        if (current.controlRevision()!=expectedRevision) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.STALE_REVISION);
        if (current.quarantined()) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.QUARANTINED);
        if (current.receiptRelation()==ReceiptRelation.CONFLICTING) throw new RecoveryPreconditionException(RecoveryPreconditionException.Code.RECEIPT_CONFLICT);
    }
}
