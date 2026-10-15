package capstone.coverstory.support;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class CAP119InstructorInfrastructureTest {
    private static final String FIXTURE_HASH = "2b6fa2a4f90928e6d55eb9221cd22f613dfae2a53aa98dc4b0f2b2b272a0fbf8";

    @Test void lockedSyntheticFixtureAndIndependentOracleEnforceBothBoundaries() throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-119.json"));
        assertEquals(FIXTURE_HASH, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        JsonNode fixture = new ObjectMapper().readTree(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
        assertFalse(fixture.path("network_enabled").asBoolean());
        assertEquals("32c37bb12dcf3c4b24efe54b9d69e710377fad1f713366d52258a52b20e96da0",
                fixture.path("source_contract_sha256").asText());

        Map<String, CAP119ReferenceOracle.Principal> principals = Map.of(
                "owner-a", new CAP119ReferenceOracle.Principal("owner-a", CAP119ReferenceOracle.Role.OWNER, "owner-A"),
                "support-a-assigned", new CAP119ReferenceOracle.Principal("support-a-assigned", CAP119ReferenceOracle.Role.SUPPORT, null),
                "support-unassigned", new CAP119ReferenceOracle.Principal("support-unassigned", CAP119ReferenceOracle.Role.SUPPORT, null));
        Map<String, CAP119ReferenceOracle.Source> sourceById = new HashMap<>();
        fixture.path("sources").forEach(n -> sourceById.put(n.path("id").asText(), new CAP119ReferenceOracle.Source(
                n.path("id").asText(), n.path("owner_id").asText(), n.path("case_id").asText(),
                n.path("kind").asText(), n.path("text").asText())));
        CAP119ReferenceOracle oracle = new CAP119ReferenceOracle();

        for (JsonNode c : fixture.path("expected_cases")) {
            AtomicInteger lookups = new AtomicInteger();
            String caseId = c.path("case_id").asText();
            var principal = principals.get(c.path("principal").asText());
            var request = new CAP119ReferenceOracle.Request(caseId,
                    c.path("requested_owner_id").isNull() ? null : c.path("requested_owner_id").asText());
            var result = oracle.retrieve(principal, request,
                    (principalId, assignedCase) -> principalId.equals("support-a-assigned") && assignedCase.equals("case-A") ? "owner-A" : null,
                    (ownerId, candidateCase) -> {
                        lookups.incrementAndGet();
                        if (c.path("adapter").asText().equals("faulty-returns-foreign")) {
                            return List.of(sourceById.get("policy-A"), sourceById.get("policy-B"));
                        }
                        return sourceById.values().stream().filter(s -> s.ownerId().equals(ownerId)
                                && s.caseId().equals(candidateCase)).toList();
                    });
            assertEquals(c.path("expected_status").asText(), result.status(), c.path("id").asText());
            assertEquals(c.path("expected_lookup_count").asInt(), lookups.get(), c.path("id").asText());
            assertEquals(c.path("expected_lookup_count").asInt(), result.lookupCount(), c.path("id").asText());
            if (c.has("expected_code")) assertEquals(c.path("expected_code").asText(), result.code(), c.path("id").asText());
            Set<String> expected = new LinkedHashSet<>();
            c.path("expected_source_ids").forEach(id -> expected.add(id.asText()));
            assertEquals(expected, result.responseSources().stream().map(CAP119ReferenceOracle.Source::id).collect(java.util.stream.Collectors.toSet()));
            boolean canary = result.responseSources().stream().anyMatch(s -> s.text().contains("BETA_CANARY"))
                    || result.modelSources().stream().anyMatch(s -> s.text().contains("BETA_CANARY"));
            assertEquals(c.path("expected_model_canary").asBoolean(), canary, c.path("id").asText());
            assertEquals(result.responseSources(), result.modelSources(), "model and response boundary must share only checked rows");
        }
    }

    @Test void supportAssignmentIsCheckedBeforeLookupAndForeignAdapterRowsAreRejected() {
        var oracle = new CAP119ReferenceOracle();
        var assigned = new CAP119ReferenceOracle.Principal("support-a-assigned", CAP119ReferenceOracle.Role.SUPPORT, null);
        AtomicInteger reads = new AtomicInteger();
        var denied = oracle.retrieve(new CAP119ReferenceOracle.Principal("support-unassigned",
                        CAP119ReferenceOracle.Role.SUPPORT, null),
                new CAP119ReferenceOracle.Request("case-A", null), (id, c) -> null,
                (owner, c) -> { reads.incrementAndGet(); return List.of(); });
        assertEquals("SUPPORT_NOT_ASSIGNED", denied.code());
        assertEquals(0, reads.get());

        var foreign = new CAP119ReferenceOracle.Source("policy-B", "owner-B", "case-B", "POLICY", "BETA_CANARY");
        var rejected = oracle.retrieve(assigned, new CAP119ReferenceOracle.Request("case-A", null),
                (id, c) -> "owner-A", (owner, c) -> List.of(foreign));
        assertEquals("RESULT_SCOPE_VIOLATION", rejected.code());
        assertTrue(rejected.responseSources().isEmpty());
        assertTrue(rejected.modelSources().isEmpty());
    }
}
