package capstone.coverstory.support;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CAP121InstructorInfrastructureTest {
    private static final String FIXTURE_SHA256 = "3637cd41563ac6b1c71303b88c87401011e886ba1f26eb20f658c84bc09c0ce2";

    @Test void frozenFixtureHashesAndByteOffsetsMatchSourceText() throws Exception {
        byte[] raw = Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-121.json"));
        assertEquals(FIXTURE_SHA256, sha(raw));
        JsonNode f = new ObjectMapper().readTree(raw);
        assertFalse(f.path("network_enabled").asBoolean());
        assertEquals("2f29dcc8486c9360a293928992260ef54f8cc30f9a7b6ceef6bd188439bdde60",
                f.path("source_contract_sha256").asText());
        for (JsonNode version : f.path("source_versions")) {
            byte[] bytes = version.path("text").asText().getBytes(StandardCharsets.UTF_8);
            assertEquals(version.path("content_sha256").asText(), sha(bytes), version.path("version_id").asText());
            assertTrue(bytes.length > 39);
        }
        for (JsonNode citation : f.path("citations")) {
            JsonNode v = findVersion(f, citation.path("source_id").asText(), citation.path("version_id").asText());
            byte[] bytes = v.path("text").asText().getBytes(StandardCharsets.UTF_8);
            int start = citation.path("start_byte").asInt(), end = citation.path("end_byte").asInt();
            assertEquals("outpatient limit", new String(bytes, start, end - start, StandardCharsets.UTF_8));
            assertEquals(v.path("content_sha256").asText(), citation.path("expected_content_sha256").asText());
        }
    }

    @Test void independentOracleEnforcesExactChainNoFallbackOwnershipAndZeroWrites() throws Exception {
        JsonNode f = new ObjectMapper().readTree(Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-121.json")));
        List<CAP121ReferenceOracle.Citation> citations = new ArrayList<>();
        for (JsonNode c : f.path("citations")) citations.add(new CAP121ReferenceOracle.Citation(c.path("id").asText(),
                c.path("owner_id").asText(), c.path("source_id").asText(), c.path("version_id").asText(),
                c.path("batch_id").asText(), c.path("evidence_id").asText(), c.path("start_byte").asInt(),
                c.path("end_byte").asInt(), c.path("expected_content_sha256").asText()));
        List<CAP121ReferenceOracle.Version> allVersions = new ArrayList<>();
        for (JsonNode v : f.path("source_versions")) allVersions.add(new CAP121ReferenceOracle.Version(
                v.path("source_id").asText(), v.path("version_id").asText(), v.path("owner_id").asText(),
                v.path("batch_id").asText(), v.path("evidence_id").asText(), v.path("text").asText(),
                v.path("content_sha256").asText()));
        var oracle = new CAP121ReferenceOracle();

        var full = oracle.resolve(new CAP121ReferenceOracle.Principal("owner-A", "owner-A"), "ai1",
                citations, allVersions, List.of("pA"));
        assertEquals("FOUND", full.status());
        assertEquals(List.of("policy-A", "v2", "m2", "pA"), List.of(full.sourceId(), full.versionId(), full.batchId(), full.evidenceId()));
        assertEquals(List.of(23, 39), List.of(full.startByte(), full.endByte()));
        assertEquals("POLICY_FACT", full.evidenceClass());
        assertEquals(0, full.writeCount());

        var withoutV2 = allVersions.stream().filter(v -> !v.sourceId().equals("policy-A") || !v.versionId().equals("v2")).toList();
        var broken = oracle.resolve(new CAP121ReferenceOracle.Principal("owner-A", "owner-A"), "ai1",
                citations, withoutV2, List.of("pA"));
        assertEquals("LINEAGE_BROKEN", broken.status());
        assertEquals("EXACT_VERSION_MISSING", broken.code());
        assertNull(broken.versionId(), "must not disclose/substitute v1 as the requested v2");

        var foreign = oracle.resolve(new CAP121ReferenceOracle.Principal("owner-A", "owner-A"), "bi1",
                citations, allVersions, List.of("pA"));
        assertEquals("NOT_FOUND", foreign.status());
        assertEquals("Citation not found.", foreign.safeMessage());
        assertNull(foreign.sourceId());
        assertNull(foreign.versionId());
        assertNull(foreign.batchId());
        assertNull(foreign.evidenceId());
    }

    private static JsonNode findVersion(JsonNode fixture, String source, String version) {
        for (JsonNode node : fixture.path("source_versions"))
            if (node.path("source_id").asText().equals(source) && node.path("version_id").asText().equals(version)) return node;
        throw new AssertionError("missing fixture version " + source + "/" + version);
    }
    private static String sha(byte[] bytes) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
}
