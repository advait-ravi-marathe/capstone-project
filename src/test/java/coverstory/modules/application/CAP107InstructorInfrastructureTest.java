package coverstory.modules.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Independent CAP-107 reference checks. Never invokes the learner seam. */
class CAP107InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-107/synthetic-v1.json");
    private enum Status { VALID_REVIEW_DESCRIPTOR, CONFLICT, UNSUPPORTED_SCHEMA, QUOTE_UNAVAILABLE, REQUOTE_REQUIRED, DRAFT, INVALID }
    private record Input(String id, String digest, int rowCount, int schema, String totalField, String taxField,
                         String scenario, String currentScenario, String config, String currentConfig,
                         boolean extractionConfirmed, String disclosure, String initialQuote, String revisedQuote) { }
    private record Result(Status status, boolean stored, boolean submitReady, List<String> blockers,
                          boolean payableAdjustment, boolean automaticAcceptance, String descriptor) { }

    @Test void syntheticFixturePinsSevenSavedOutcomesAndNoExternalEffects() throws Exception {
        JsonNode fixture = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", fixture.path("classification").asText());
        assertFalse(fixture.path("actual_insurer_or_payment_calls").asBoolean());
        assertEquals(7, fixture.path("cases").size());
        assertEquals(3, fixture.path("cases").get(0).path("row_count").asInt());
        assertEquals("VALID_REVIEW_DESCRIPTOR", fixture.path("cases").get(0).path("expected").asText());
    }

    @Test void validManifestOnlyProducesDescriptorAndDoesNotReplaceInitialQuoteOrAccept() throws Exception {
        byte[] bytes = bytes();
        Result result = reference(input("m1", digest(bytes), 3, 1, "totalMinor", "taxMinor", "scenario-v1", "scenario-v1",
                "config-v1", "config-v1", true, "NO"), null, 1, Set.of("totalMinor", "taxMinor"), bytes);
        assertEquals(Status.VALID_REVIEW_DESCRIPTOR, result.status());
        assertEquals("q-initial", result.descriptor().split("\\|")[0]);
        assertTrue(result.descriptor().contains("revised=q-revised"));
        assertFalse(result.payableAdjustment()); assertFalse(result.automaticAcceptance());
    }

    @Test void changedDigestForExistingManifestIdIsConflictAndNeverStored() throws Exception {
        byte[] bytes = bytes();
        Result result = reference(input("m1", digest(bytes), 3, 1, "totalMinor", "taxMinor", "scenario-v1", "scenario-v1",
                "config-v1", "config-v1", true, "NO"), "sha256:different", 1, Set.of("totalMinor", "taxMinor"), bytes);
        assertEquals(Status.CONFLICT, result.status()); assertFalse(result.stored());
        assertEquals(Status.VALID_REVIEW_DESCRIPTOR, reference(input("m1", digest(bytes), 3, 1, "totalMinor", "taxMinor",
                "scenario-v1", "scenario-v1", "config-v1", "config-v1", true, "NO"), digest(bytes), 1,
                Set.of("totalMinor", "taxMinor"), bytes).status());
    }

    @Test void unsupportedSchemaOrUnknownMoneyFieldDoesNotManufacturePayableQuote() throws Exception {
        byte[] bytes = bytes();
        Result schema = reference(input("m2", digest(bytes), 3, 99, "totalMinor", "taxMinor", "s", "s", "c", "c", true, "NO"),
                null, 1, Set.of("totalMinor", "taxMinor"), bytes);
        Result field = reference(input("m3", digest(bytes), 3, 1, "unknownTotal", "taxMinor", "s", "s", "c", "c", true, "NO"),
                null, 1, Set.of("totalMinor", "taxMinor"), bytes);
        assertEquals(Status.UNSUPPORTED_SCHEMA, schema.status()); assertEquals(Status.QUOTE_UNAVAILABLE, field.status());
        assertFalse(schema.payableAdjustment()); assertFalse(field.payableAdjustment());
    }

    @Test void staleScenarioOrConfigurationHashForChangedOptionsRequiresRequote() throws Exception {
        byte[] bytes = bytes();
        Result result = reference(input("m4", digest(bytes), 3, 1, "totalMinor", "taxMinor", "s-old", "s-new", "c-old", "c-new", true, "NO"),
                null, 1, Set.of("totalMinor", "taxMinor"), bytes);
        assertEquals(Status.REQUOTE_REQUIRED, result.status()); assertFalse(result.submitReady());
    }

    @Test void extractionAndDisclosureBlockersSaveDraftWithoutSubmitReadiness() throws Exception {
        byte[] bytes = bytes();
        Result extraction = reference(input("m5", digest(bytes), 3, 1, "totalMinor", "taxMinor", "s", "s", "c", "c", false, "NO"),
                null, 1, Set.of("totalMinor", "taxMinor"), bytes);
        Result disclosure = reference(input("m6", digest(bytes), 3, 1, "totalMinor", "taxMinor", "s", "s", "c", "c", true, "UNSURE"),
                null, 1, Set.of("totalMinor", "taxMinor"), bytes);
        assertEquals(Status.DRAFT, extraction.status()); assertTrue(extraction.stored()); assertFalse(extraction.submitReady());
        assertEquals(List.of("EXTRACTION_UNCONFIRMED"), extraction.blockers());
        assertEquals(Status.DRAFT, disclosure.status()); assertTrue(disclosure.stored()); assertFalse(disclosure.submitReady());
        assertEquals(List.of("DISCLOSURE_UNSURE"), disclosure.blockers());
    }

    @Test void digestRowCountAndBlankIdentityAreRejectedBeforeDescriptor() throws Exception {
        byte[] bytes = bytes();
        assertNotEquals(Status.VALID_REVIEW_DESCRIPTOR, reference(input("m", "sha256:bad", 3, 1, "totalMinor", "taxMinor", "s", "s", "c", "c", true, "NO"),
                null, 1, Set.of("totalMinor", "taxMinor"), bytes).status());
        assertNotEquals(Status.VALID_REVIEW_DESCRIPTOR, reference(input("m", digest(bytes), 2, 1, "totalMinor", "taxMinor", "s", "s", "c", "c", true, "NO"),
                null, 1, Set.of("totalMinor", "taxMinor"), bytes).status());
        assertEquals(Status.INVALID, reference(input(" ", digest(bytes), 3, 1, "totalMinor", "taxMinor", "s", "s", "c", "c", true, "NO"),
                null, 1, Set.of("totalMinor", "taxMinor"), bytes).status());
    }

    private static Result reference(Input m, String priorDigest, int supportedSchema, Set<String> knownFields, byte[] exactBytes) throws Exception {
        if (m.id() == null || m.id().isBlank() || m.digest() == null || m.rowCount() < 0) return result(Status.INVALID, false, false, List.of());
        if (priorDigest != null && !priorDigest.equals(m.digest())) return result(Status.CONFLICT, false, false, List.of());
        if (m.schema() != supportedSchema) return result(Status.UNSUPPORTED_SCHEMA, false, false, List.of());
        if (!knownFields.contains(m.totalField()) || !knownFields.contains(m.taxField())) return result(Status.QUOTE_UNAVAILABLE, false, false, List.of());
        if (!m.scenario().equals(m.currentScenario()) || !m.config().equals(m.currentConfig())) return result(Status.REQUOTE_REQUIRED, false, false, List.of());
        if (!m.digest().equals(digest(exactBytes)) || countRows(exactBytes) != m.rowCount()) return result(Status.INVALID, false, false, List.of());
        List<String> blockers = new ArrayList<>();
        if (!m.extractionConfirmed()) blockers.add("EXTRACTION_UNCONFIRMED");
        if ("UNSURE".equals(m.disclosure())) blockers.add("DISCLOSURE_UNSURE");
        if (!blockers.isEmpty()) return result(Status.DRAFT, true, false, blockers);
        return new Result(Status.VALID_REVIEW_DESCRIPTOR, true, true, List.of(), false, false,
                m.initialQuote() + "|revised=" + m.revisedQuote());
    }
    private static Result result(Status status, boolean stored, boolean ready, List<String> blockers) {
        return new Result(status, stored, ready, List.copyOf(blockers), false, false, null);
    }
    private static Input input(String id, String hash, int rows, int schema, String total, String tax,
                               String scenario, String currentScenario, String config, String currentConfig,
                               boolean confirmed, String disclosure) {
        return new Input(id, hash, rows, schema, total, tax, scenario, currentScenario, config, currentConfig,
                confirmed, disclosure, "q-initial", "q-revised");
    }
    private static byte[] bytes() { return "{\"rows\":[{\"id\":\"A\"},{\"id\":\"B\"},{\"id\":\"C\"}],\"totalMinor\":125000,\"taxMinor\":0}".getBytes(StandardCharsets.UTF_8); }
    private static int countRows(byte[] bytes) { return new String(bytes, StandardCharsets.UTF_8).split("\\\"id\\\"").length - 1; }
    private static String digest(byte[] bytes) throws Exception { return "sha256:" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
}
