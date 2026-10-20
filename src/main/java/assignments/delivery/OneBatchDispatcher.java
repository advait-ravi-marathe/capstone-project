package assignments.delivery;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

/** Explicit, sequential capture and delivery invocation. It never runs automatically. */
public final class OneBatchDispatcher {
    private final IssuanceWorkStore store;
    private final TransactionTemplate captureTransaction;
    private final TransactionTemplate receiptTransaction;
    private final TransactionTemplate acknowledgmentTransaction;

    public OneBatchDispatcher(IssuanceWorkStore store, PlatformTransactionManager manager) {
        this.store = Objects.requireNonNull(store);
        this.captureTransaction = writeTemplate(manager);
        this.receiptTransaction = writeTemplate(manager);
        this.acknowledgmentTransaction = writeTemplate(manager);
    }

    private static TransactionTemplate writeTemplate(PlatformTransactionManager manager) {
        TransactionTemplate template = new TransactionTemplate(manager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.setReadOnly(false);
        return template;
    }

    public BatchDeliveryReport runOnce(int limit) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Delivery batch cannot join an ambient transaction");
        }
        // LEARNER TODO Day 106: capture and drain sequentially with separately committed receipt
        // and acknowledgment transactions. Stop at first failure; never auto-retry or skip.
        throw new UnsupportedOperationException("Day 106 learner TODO: orchestrate one batch");
    }
}
