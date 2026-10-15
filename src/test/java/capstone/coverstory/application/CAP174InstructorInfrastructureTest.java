package capstone.coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

/** Independent fixed-vector oracle; it does not call the learner seam. */
class CAP174InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-174/synthetic-v1.json");
    private final ObjectMapper json = new ObjectMapper();

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    @Test void fixtureIsOfflineSyntheticAndCoversSavedVectors() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_PROPOSAL_DIGEST", root.path("classification").asText());
        assertFalse(root.path("actual_customer_data").asBoolean());
        assertFalse(root.path("actual_submission").asBoolean());
        assertFalse(root.path("actual_payment_or_provider_call").asBoolean());
        assertEquals(6, root.path("cases").size());
    }

    @Test void exactBytesAndNewlineHaveDifferentDigests() throws Exception {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", sha256("abc".getBytes(java.nio.charset.StandardCharsets.US_ASCII)));
        assertEquals("edeaaff3f1774ad2888673770c6d64097e391bc362d7d6fb34982ddf0efd18cb", sha256("abc\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII)));
    }

    @Test void chunkingDoesNotChangeDigestAndEmptyBytesHaveKnownVector() throws Exception {
        byte[] joined = "abcd".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] left = "ab".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] right = "cd".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] concatenated = new byte[left.length + right.length];
        System.arraycopy(left, 0, concatenated, 0, left.length);
        System.arraycopy(right, 0, concatenated, left.length, right.length);
        assertEquals("88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589", sha256(joined));
        assertEquals(sha256(joined), sha256(concatenated));
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", sha256(new byte[0]));
    }

    @Test void malformedDigestAndRevisionDriftAreDistinctGates() {
        assertFalse("xyz".matches("(?i)[0-9a-f]{64}"));
        assertTrue("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad".matches("(?i)[0-9a-f]{64}"));
        String retainedSnapshotVersion = "1", currentProposalVersion = "2";
        assertNotEquals(retainedSnapshotVersion, currentProposalVersion);
    }

    @Test void fixtureVectorsAreIndependentlyRecomputed() throws Exception {
        JsonNode cases = json.readTree(Files.readString(FIXTURE)).path("cases");
        for (JsonNode c : cases) {
            byte[] bytes = HexFormat.of().parseHex(c.path("bytes_hex").asText());
            String actual = sha256(bytes);
            String expected = c.path("expected_sha256").asText();
            if (expected.matches("(?i)[0-9a-f]{64}")) {
                if ("DIGEST_MISMATCH".equals(c.path("expected").asText())) {
                    assertNotEquals(expected, actual, c.path("name").asText());
                } else {
                    assertEquals(expected, actual, c.path("name").asText());
                }
            } else {
                assertEquals("INVALID_MANIFEST", c.path("expected").asText());
            }
        }
    }
}
