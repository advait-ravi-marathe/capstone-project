package capstone.coverstory;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Independent fixture/oracle scaffolds for the three absent Day 48/49/56 instructor records. */
class Day48To56InstructorScaffoldTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void cap87SyntheticResumeVectorRejectsBrowserAuthorityAndMissingKey() throws Exception {
        JsonNode root = read("CAP-87");
        assertEquals("SYNTHETIC_PAID_ONBOARDING_RESUME", root.path("classification").asText());
        assertEquals(100000, root.path("payment").path("amount_minor").asLong());
        assertEquals("CONFIRMED", root.path("payment").path("status").asText());
        assertFalse(root.path("protected_payload").path("plaintext_in_journey").asBoolean());
        assertEquals(root.path("application_a").asText(), root.path("protected_payload").path("aad_object_id").asText());
        assertEquals(4, root.path("protected_payload").path("aad_revision").asInt());
        assertEquals("ISSUED", root.path("journey_step").path("browser_claim").asText());
        assertEquals(20, root.path("analytics_queue").path("capacity").asInt());
        assertEquals(3, root.path("analytics_queue").path("retry_limit").asInt());
        assertEquals(7, root.path("cases").size());
        assertEquals("KEY_UNAVAILABLE retains ciphertext and does not fabricate decrypted draft",
                root.path("cases").get(6).path("expected").asText());
    }

    @Test void cap171ExactVersionAndOwnerOracleRetiresAnswerButPreservesQuoteHistory() throws Exception {
        JsonNode root = read("CAP-171");
        JsonNode active = root.path("active_source"), retired = root.path("retired_source");
        assertEquals(active.path("source_id").asText(), retired.path("source_id").asText());
        assertNotEquals(active.path("version").asText(), retired.path("version").asText());
        assertTrue(active.path("active").asBoolean()); assertFalse(retired.path("active").asBoolean());
        assertFalse(isUsable(retired, UUID.fromString(root.path("requester_a").asText())));
        assertFalse(isUsable(root.path("private_source"), UUID.fromString(root.path("requester_b").asText())));
        assertTrue(isUsable(active, UUID.fromString(root.path("requester_b").asText())));
        assertTrue(root.path("cases").get(4).path("expected").asText().contains("history"));
        assertEquals(5, root.path("cases").size());
    }

    @Test void cap172ExactDecisionOracleRequiresBothReadsAndDenyWins() throws Exception {
        JsonNode root = read("CAP-172");
        UUID owner = UUID.fromString(root.path("owner_a").asText());
        UUID journey = UUID.fromString(root.path("journey_a").asText());
        UUID revision = UUID.fromString(root.path("revision_a").asText());
        JsonNode decisions = root.path("decisions");
        tools.jackson.databind.node.ArrayNode allowOnly = JSON.createArrayNode();
        decisions.forEach(row -> { if (!("DENY".equals(row.path("effect").asText()) && "READ_JOURNEY".equals(row.path("action").asText()))) allowOnly.add(row); });
        assertTrue(exactAllowed(allowOnly, owner, "READ_JOURNEY", journey));
        assertFalse(exactAllowed(decisions, owner, "READ_JOURNEY", journey), "exact deny overrides the otherwise matching allow");
        assertTrue(exactAllowed(decisions, owner, "READ_PROTECTED_REVISION", revision));
        assertFalse(exactAllowed(decisions, owner, "READ_JOURNEY", UUID.randomUUID()));
        assertFalse(root.path("wildcards_supported").asBoolean());
        assertFalse(root.path("conditional_policies_supported").asBoolean());
        assertEquals(6, root.path("cases").size());
    }

    private static boolean isUsable(JsonNode source, UUID requester) {
        if (!source.path("active").asBoolean()) return false;
        String owner = source.path("owner_id").asText("");
        return owner.isBlank() || owner.equals(requester.toString());
    }

    private static boolean exactAllowed(JsonNode decisions, UUID principal, String action, UUID resource) {
        boolean allow = false, deny = false;
        for (JsonNode row : decisions) {
            if (principal.toString().equals(alias(row.path("principal").asText()))
                    && action.equals(row.path("action").asText())
                    && resource.toString().equals(alias(row.path("resource").asText()))) {
                allow |= "ALLOW".equals(row.path("effect").asText());
                deny |= "DENY".equals(row.path("effect").asText());
            }
        }
        return allow && !deny;
    }

    private static String alias(String name) {
        return switch (name) {
            case "owner_a" -> "a1720000-0000-4000-8000-000000000001";
            case "owner_b" -> "b1720000-0000-4000-8000-000000000002";
            case "journey_a" -> "a1730000-0000-4000-8000-000000000001";
            case "revision_a" -> "a1740000-0000-4000-8000-000000000001";
            default -> name;
        };
    }

    private static JsonNode read(String cap) throws Exception {
        return JSON.readTree(Files.readString(Path.of("fixtures/coverstory", cap, "synthetic-v1.json")));
    }
}
