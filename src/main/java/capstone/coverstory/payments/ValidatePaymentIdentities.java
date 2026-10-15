package capstone.coverstory.payments;

import java.util.List;

/** CAP-170 learner seam. This validates synthetic identities only; it does not replay payment effects. */
public final class ValidatePaymentIdentities {
    /**
     * Validate exact (owner, event, epoch, attemptNo) identity and increasing attempt order.
     *
     * LEARNER TODO: reject invalid/null domains, duplicate composite keys, first attempts that
     * do not start at one, and non-increasing attempts within an owner/event/epoch.
     */
    public AttemptLedgerReport validateAttemptLedger(List<ReconciliationAttempt> rows) {
        throw new UnsupportedOperationException("CAP-170 learner TODO: implement attempt ledger validation");
    }
}
