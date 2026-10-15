package capstone.coverstory.application;

import java.util.List;
import java.util.Map;

/** Learner-owned CAP-110 seam. The instructor oracle lives only under test sources. */
public final class ValidateSafeConfiguration {
    private ValidateSafeConfiguration() { }

    public record ProviderSettings(String mode, int timeoutMillis, int inputBudget, int outputBudget,
                                   boolean secretConfigured) { }
    public record ReviewState(boolean ownerVerified, boolean requiredFieldsComplete,
                              boolean disclosuresComplete, boolean extractionConfirmed,
                              boolean exactRevisionReviewed, boolean balanceReconciled,
                              boolean refundsReconciled, boolean explicitAction) { }
    public record Result(boolean valid, String code, List<String> blockers,
                         Map<String, Object> safeManifest, boolean submissionPending,
                         boolean dispatchCreated) { }

    public static Result validate(ProviderSettings settings, Map<String, String> extraConfiguration,
                                  ReviewState review) {
        // TODO(CAP-110): reject unsafe effective configuration and evaluate the supplied
        // explicit-submission readiness policy without logging secrets or creating effects.
        throw new UnsupportedOperationException("TODO(CAP-110): validate safe configuration and final-review gate");
    }
}
