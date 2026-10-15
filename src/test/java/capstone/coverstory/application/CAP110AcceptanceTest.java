package capstone.coverstory.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP110AcceptanceTest {
    @Test void validConfigurationAndSecretRedaction() {
        var result = ValidateSafeConfiguration.validate(
                new ValidateSafeConfiguration.ProviderSettings("mock", 1500, 2000, 256, true), Map.of(),
                new ValidateSafeConfiguration.ReviewState(true, true, true, true, true, true, true, false));
        assertTrue(result.valid());
        assertFalse(result.safeManifest().toString().contains("SECRET_SENTINEL"));
    }
    @Test void unsafeConfigurationIsRejectedBeforeProviderUse() {
        var result = ValidateSafeConfiguration.validate(
                new ValidateSafeConfiguration.ProviderSettings("live", 0, 0, -1, false), Map.of("skipReconciliation", "true"),
                new ValidateSafeConfiguration.ReviewState(true, true, true, true, true, true, true, true));
        assertFalse(result.valid());
        assertFalse(result.submissionPending() || result.dispatchCreated());
    }
    @Test void staleOrIncompleteReviewRemainsResumableAndNeverSubmits() {
        var result = ValidateSafeConfiguration.validate(
                new ValidateSafeConfiguration.ProviderSettings("mock", 1500, 2000, 256, false), Map.of(),
                new ValidateSafeConfiguration.ReviewState(true, false, false, false, false, false, false, false));
        assertFalse(result.valid());
        assertFalse(result.submissionPending() || result.dispatchCreated());
    }
    @Test void todoGuardKeepsBusinessLogicUnimplemented() throws Exception {
        var method = ValidateSafeConfiguration.class.getMethod("validate", ValidateSafeConfiguration.ProviderSettings.class,
                Map.class, ValidateSafeConfiguration.ReviewState.class);
        assertNotNull(method);
    }
}
