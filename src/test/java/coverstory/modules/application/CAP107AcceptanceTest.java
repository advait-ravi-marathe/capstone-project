package coverstory.modules.application;

import coverstory.modules.application.api.CAP107Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Learner contract. Failures at the explicit TODO are expected until implemented. */
@Tag("learnerAcceptance")
class CAP107AcceptanceTest {
    private static final byte[] BYTES = "{\"rows\":[{\"id\":\"A\"},{\"id\":\"B\"},{\"id\":\"C\"}],\"totalMinor\":125000,\"taxMinor\":0}"
            .getBytes(StandardCharsets.UTF_8);
    private static final CAP107Seam.SupportedSchema SCHEMA = new CAP107Seam.SupportedSchema(1, List.of("totalMinor", "taxMinor"));

    @Test void exactThreeRowManifestYieldsDescriptorWithoutAcceptingOrAdjustingMoney() throws Exception {
        var result = validate(manifest("m1", digest(BYTES), 3, 1, "totalMinor", "taxMinor", true, "NO", "scenario-v1", "scenario-v1", "config-v1", "config-v1"), null);
        assertEquals(CAP107Seam.Status.VALID_REVIEW_DESCRIPTOR, result.status());
        assertNotNull(result.reviewDescriptor());
        assertFalse(result.automaticAcceptance());
        assertFalse(result.payableAdjustment());
    }

    @Test void sameManifestIdWithDifferentDigestConflictsWithoutOverwrite() throws Exception {
        var result = validate(manifest("m1", digest(BYTES), 3, 1, "totalMinor", "taxMinor", true, "NO", "scenario-v1", "scenario-v1", "config-v1", "config-v1"), "sha256:different");
        assertEquals(CAP107Seam.Status.CONFLICT, result.status());
        assertFalse(result.draftStored());
    }

    @Test void unsupportedSchemaAndUnknownRequiredMoneyFieldNeverAdjustPayable() throws Exception {
        var unsupported = validate(manifest("m2", digest(BYTES), 3, 99, "totalMinor", "taxMinor", true, "NO", "scenario-v1", "scenario-v1", "config-v1", "config-v1"), null);
        var unknown = validate(manifest("m3", digest(BYTES), 3, 1, "unknownTotal", "taxMinor", true, "NO", "scenario-v1", "scenario-v1", "config-v1", "config-v1"), null);
        assertEquals(CAP107Seam.Status.UNSUPPORTED_SCHEMA, unsupported.status());
        assertEquals(CAP107Seam.Status.QUOTE_UNAVAILABLE, unknown.status());
        assertFalse(unsupported.payableAdjustment()); assertFalse(unknown.payableAdjustment());
    }

    @Test void changedScenarioOrOptionsConfigurationRequiresRequote() throws Exception {
        var result = validate(manifest("m4", digest(BYTES), 3, 1, "totalMinor", "taxMinor", true, "NO", "scenario-v1", "scenario-v2", "config-v1", "config-v2"), null);
        assertEquals(CAP107Seam.Status.REQUOTE_REQUIRED, result.status());
        assertFalse(result.submitReady());
    }

    @Test void unconfirmedExtractionAndUnsureDisclosureRemainDraftWithExactBlockers() throws Exception {
        var extraction = validate(manifest("m5", digest(BYTES), 3, 1, "totalMinor", "taxMinor", false, "NO", "scenario-v1", "scenario-v1", "config-v1", "config-v1"), null);
        var disclosure = validate(manifest("m6", digest(BYTES), 3, 1, "totalMinor", "taxMinor", true, "UNSURE", "scenario-v1", "scenario-v1", "config-v1", "config-v1"), null);
        assertTrue(extraction.draftStored()); assertFalse(extraction.submitReady());
        assertEquals(List.of("EXTRACTION_UNCONFIRMED"), extraction.blockers());
        assertTrue(disclosure.draftStored()); assertFalse(disclosure.submitReady());
        assertEquals(List.of("DISCLOSURE_UNSURE"), disclosure.blockers());
    }

    private static CAP107Seam.ManifestResult validate(CAP107Seam.ReviewManifest m, String existing) throws Exception {
        return new CAP107Seam().validateReviewManifest(m, new CAP107Seam.ExactBytes(BYTES, existing), SCHEMA);
    }
    private static CAP107Seam.ReviewManifest manifest(String id, String digest, int rows, int schema, String total, String tax,
                                                        boolean confirmed, String disclosure, String suppliedScenario,
                                                        String currentScenario, String suppliedConfig, String currentConfig) {
        return new CAP107Seam.ReviewManifest(id, digest, rows, schema, total, tax, suppliedScenario, currentScenario,
                suppliedConfig, currentConfig, confirmed, disclosure, "q-initial", "q-revised");
    }
    static String digest(byte[] bytes) throws Exception {
        return "sha256:" + java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
