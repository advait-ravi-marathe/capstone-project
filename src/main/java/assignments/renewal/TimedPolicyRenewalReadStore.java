package assignments.renewal;

import assignments.insurance.PolicyRenewalFacts;
import assignments.insurance.PolicyRenewalReadStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

/** Transaction-owning decorator placed inside the existing outer bounded admission decorator. */
public final class TimedPolicyRenewalReadStore implements PolicyRenewalReadStore {
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final PolicyRenewalReadStore delegate;
    private final PreviewReadBudgetPolicy budgets;

    public TimedPolicyRenewalReadStore(TransactionTemplate transactionTemplate,
                                       JdbcTemplate jdbcTemplate,
                                       PolicyRenewalReadStore delegate,
                                       PreviewReadBudgetPolicy budgets) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.delegate = delegate;
        this.budgets = budgets;
        if (transactionTemplate.getPropagationBehavior() != TransactionDefinition.PROPAGATION_REQUIRED)
            throw new IllegalArgumentException("The supplied template must be REQUIRED; this decorator owns the top-level boundary");
        if (!transactionTemplate.isReadOnly())
            throw new IllegalArgumentException("The supplied template must enforce read-only transactions");
    }

    @Override
    public Optional<PolicyRenewalFacts> findByPolicyIdAndOwnerId(String policyId, String ownerId) {
        // LEARNER TODO Day 87/89/90: reject an ambient transaction before any database mutation,
        // run exactly once in this top-level template, set local budgets, invoke the unchanged
        // owner-scoped delegate exactly once, preserve Optional.empty/present, and allow all
        // mapping/cleanup failures to roll back. Translate only the Day 92 SQL-state cases.
        throw new UnsupportedOperationException("Day 89 learner TODO: own one bounded read transaction");
    }

    boolean hasAmbientTransactionForTests() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
}
