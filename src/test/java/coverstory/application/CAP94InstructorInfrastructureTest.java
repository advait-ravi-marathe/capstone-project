package coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/** Independent test-only oracle. It reads fixture bytes and never runs a build. */
class CAP94InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-94/synthetic-v1.json");
    private static final ObjectMapper JSON = new ObjectMapper();

    @TempDir Path temp;

    @Test void syntheticFixtureIsExplicitlyNotARealBuild() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("builds_executed").asBoolean());
        assertEquals(6, root.path("cases").size());
        assertTrue(root.path("cases").findValues("expected").stream().anyMatch(node -> node.asText().equals("BLOCKED")));
    }

    @Test void sameRawBytesAndCompleteIdenticalProvenanceAreReproducible() throws Exception {
        Path a = artifact("coverstory.jar", "synthetic archive bytes\n");
        Path b = artifact("coverstory.jar", "synthetic archive bytes\n");
        var result = oracle(evidence("a", "commit-001", a, report(42, 42, 0, 0)),
                evidence("b", "commit-001", b, report(42, 42, 0, 0)));
        assertEquals(CAP94Seam.Status.REPRODUCIBLE, result.status());
        assertEquals(hash(Files.readAllBytes(a)), result.leftSha256());
        assertEquals(result.leftSha256(), result.rightSha256());
        assertTrue(result.testCountsMatch());
    }

    @Test void timestampOnlyArchiveBytesRemainDifferentAndFail() throws Exception {
        Path a = artifact("first.jar", "PK\u0003\u0004mtime=2026-10-14T10:00:00Z\nbody\n");
        Path b = artifact("second.jar", "PK\u0003\u0004mtime=2026-10-14T10:00:01Z\nbody\n");
        var result = oracle(evidence("a", "commit-001", a, report(42, 42, 0, 0)),
                evidence("b", "commit-001", b, report(42, 42, 0, 0)));
        assertEquals(CAP94Seam.Status.FAIL, result.status());
        assertNotEquals(result.leftSha256(), result.rightSha256());
        assertTrue(result.explanation().contains("coverstory.jar"));
        // Discriminates the forbidden normalization mutant that strips timestamp fields.
        assertEquals(hash(stripTimestamp(Files.readAllBytes(a))), hash(stripTimestamp(Files.readAllBytes(b))));
    }

    @Test void sourceCommitMismatchIsInvalidInputBeforeAnyHashBasedPass() throws Exception {
        Path a = artifact("a.jar", "identical payload");
        Path b = artifact("b.jar", "identical payload");
        var result = oracle(evidence("a", "commit-001", a, report(42, 42, 0, 0)),
                evidence("b", "commit-002", b, report(42, 42, 0, 0)));
        assertEquals(CAP94Seam.Status.INVALID_INPUT, result.status());
        assertTrue(result.explanation().contains("commit"));
    }

    @Test void absentManifestReportOrArtifactIsBlockedAndNeverInventsSuccess() throws Exception {
        Path existing = artifact("present.jar", "synthetic payload");
        var complete = evidence("a", "commit-001", existing, report(42, 42, 0, 0));
        assertEquals(CAP94Seam.Status.BLOCKED, oracle(null, complete).status());
        assertEquals(CAP94Seam.Status.BLOCKED,
                oracle(evidence("no-test-report", "commit-001", existing, null), complete).status());
        var missingPath = evidence("missing", "commit-001", temp.resolve("absent.jar"), report(42, 42, 0, 0));
        assertEquals(CAP94Seam.Status.BLOCKED, oracle(missingPath, complete).status());
    }

    @Test void toolchainConfigurationAndCommandsMustMatchExactly() throws Exception {
        Path a = artifact("a.jar", "same"); Path b = artifact("b.jar", "same");
        var left = evidence("a", "commit-001", a, report(42, 42, 0, 0));
        var right = evidence("b", "commit-001", b, report(42, 42, 0, 0));
        for (String field : java.util.List.of("javaVersion", "mavenVersion", "dependencyManifestSha256",
                "buildConfigurationSha256", "environmentFingerprint", "buildCommand", "artifactName")) {
            String different = field.endsWith("Sha256") ? "c".repeat(64) : "different-value";
            assertEquals(CAP94Seam.Status.INVALID_INPUT,
                    oracle(left, withManifest(right, replace(right.manifest(), field, different))).status(), field);
        }
        assertEquals(CAP94Seam.Status.INVALID_INPUT,
                oracle(left, withTests(right, new CAP94Seam.TestReport("./mvnw verify", 42, 42, 0, 0))).status());
    }

    @Test void testCountDifferencesAreInvalidAndRecordedFailuresFailTheGate() throws Exception {
        Path a = artifact("a.jar", "same"); Path b = artifact("b.jar", "same");
        var left = evidence("a", "commit-001", a, report(42, 42, 0, 0));
        assertEquals(CAP94Seam.Status.INVALID_INPUT, oracle(left,
                evidence("b", "commit-001", b, report(41, 41, 0, 0))).status());
        var failingLeft = evidence("a-failing", "commit-001", a, report(42, 40, 2, 0));
        var failingRight = evidence("b-failing", "commit-001", b, report(42, 40, 2, 0));
        assertEquals(CAP94Seam.Status.FAIL, oracle(failingLeft, failingRight).status());
        assertEquals(CAP94Seam.Status.BLOCKED, oracle(left,
                evidence("bad-report", "commit-001", b, report(42, 41, 0, 0))).status());
    }

    /** Independent private reference; production CAP94Seam remains the learner's work. */
    private static CAP94Seam.ReproducibilityReport oracle(CAP94Seam.BuildEvidence left,
                                                           CAP94Seam.BuildEvidence right) {
        if (left == null || right == null || left.manifest() == null || right.manifest() == null) {
            return result(CAP94Seam.Status.BLOCKED, null, null, null, false, "INCOMPLETE: manifest absent");
        }
        var lm = left.manifest(); var rm = right.manifest();
        if (present(lm.sourceCommit()) && present(rm.sourceCommit()) && !lm.sourceCommit().equals(rm.sourceCommit())) {
            return result(CAP94Seam.Status.INVALID_INPUT, null, null, lm.artifactName(), false,
                    "Source commit mismatch; artifacts are incomparable");
        }
        if (!manifestComplete(lm) || !manifestComplete(rm) || left.tests() == null || right.tests() == null) {
            return result(CAP94Seam.Status.BLOCKED, null, null, first(lm.artifactName(), rm.artifactName()), false,
                    "INCOMPLETE: manifest or test report field absent");
        }
        if (!sameProvenance(lm, rm) || !lm.artifactName().equals(rm.artifactName())
                || !left.tests().command().equals(right.tests().command())) {
            return result(CAP94Seam.Status.INVALID_INPUT, null, null, lm.artifactName(), false,
                    "Toolchain, environment, configuration, artifact identity, or command differs");
        }
        var lt = left.tests(); var rt = right.tests();
        if (!validCounts(lt) || !validCounts(rt)) {
            return result(CAP94Seam.Status.BLOCKED, null, null, lm.artifactName(), false,
                    "INCOMPLETE: test report counts are inconsistent");
        }
        boolean countsMatch = lt.total() == rt.total() && lt.passed() == rt.passed()
                && lt.failed() == rt.failed() && lt.skipped() == rt.skipped();
        if (!countsMatch) return result(CAP94Seam.Status.INVALID_INPUT, null, null, lm.artifactName(), false,
                "Test counts differ between build reports");
        try {
            if (!Files.isRegularFile(lm.artifactPath()) || !Files.isRegularFile(rm.artifactPath())) {
                return result(CAP94Seam.Status.BLOCKED, null, null, lm.artifactName(), true,
                        "INCOMPLETE: artifact file absent");
            }
            String lh = hash(Files.readAllBytes(lm.artifactPath()));
            String rh = hash(Files.readAllBytes(rm.artifactPath()));
            boolean artifactsMatch = lh.equals(rh);
            CAP94Seam.Status status = !artifactsMatch || lt.failed() != 0 || rt.failed() != 0
                    ? CAP94Seam.Status.FAIL : CAP94Seam.Status.REPRODUCIBLE;
            String text = !artifactsMatch ? "Raw artifact bytes differ for " + lm.artifactName()
                    : lt.failed() != 0 ? "Test report contains failures" : "Same raw bytes and recorded evidence";
            return result(status, lh, rh, lm.artifactName(), true, text);
        } catch (IOException | java.security.GeneralSecurityException e) {
            return result(CAP94Seam.Status.BLOCKED, null, null, lm.artifactName(), true,
                    "INCOMPLETE: artifact could not be read and hashed");
        }
    }

    private CAP94Seam.BuildEvidence evidence(String suffix, String commit, Path path, CAP94Seam.TestReport tests) {
        var m = new CAP94Seam.BuildManifest(commit, "21.0.2", "3.9.9", "a".repeat(64),
                "b".repeat(64), "linux-x86_64", "./mvnw -DskipTests package",
                "coverstory.jar", path);
        return new CAP94Seam.BuildEvidence(m, tests);
    }
    private static CAP94Seam.TestReport report(int total, int passed, int failed, int skipped) {
        return new CAP94Seam.TestReport("./mvnw test", total, passed, failed, skipped);
    }
    private Path artifact(String name, String bytes) throws IOException {
        Path path = temp.resolve(name); Files.write(path, bytes.getBytes(StandardCharsets.UTF_8)); return path;
    }
    private static CAP94Seam.BuildEvidence withManifest(CAP94Seam.BuildEvidence e, CAP94Seam.BuildManifest m) {
        return new CAP94Seam.BuildEvidence(m, e.tests());
    }
    private static CAP94Seam.BuildEvidence withTests(CAP94Seam.BuildEvidence e, CAP94Seam.TestReport t) {
        return new CAP94Seam.BuildEvidence(e.manifest(), t);
    }
    private static CAP94Seam.BuildManifest replace(CAP94Seam.BuildManifest m, String field, String value) {
        return switch (field) {
            case "javaVersion" -> new CAP94Seam.BuildManifest(m.sourceCommit(), value, m.mavenVersion(), m.dependencyManifestSha256(), m.buildConfigurationSha256(), m.environmentFingerprint(), m.buildCommand(), m.artifactName(), m.artifactPath());
            case "mavenVersion" -> new CAP94Seam.BuildManifest(m.sourceCommit(), m.javaVersion(), value, m.dependencyManifestSha256(), m.buildConfigurationSha256(), m.environmentFingerprint(), m.buildCommand(), m.artifactName(), m.artifactPath());
            case "dependencyManifestSha256" -> new CAP94Seam.BuildManifest(m.sourceCommit(), m.javaVersion(), m.mavenVersion(), value, m.buildConfigurationSha256(), m.environmentFingerprint(), m.buildCommand(), m.artifactName(), m.artifactPath());
            case "buildConfigurationSha256" -> new CAP94Seam.BuildManifest(m.sourceCommit(), m.javaVersion(), m.mavenVersion(), m.dependencyManifestSha256(), value, m.environmentFingerprint(), m.buildCommand(), m.artifactName(), m.artifactPath());
            case "environmentFingerprint" -> new CAP94Seam.BuildManifest(m.sourceCommit(), m.javaVersion(), m.mavenVersion(), m.dependencyManifestSha256(), m.buildConfigurationSha256(), value, m.buildCommand(), m.artifactName(), m.artifactPath());
            case "buildCommand" -> new CAP94Seam.BuildManifest(m.sourceCommit(), m.javaVersion(), m.mavenVersion(), m.dependencyManifestSha256(), m.buildConfigurationSha256(), m.environmentFingerprint(), value, m.artifactName(), m.artifactPath());
            case "artifactName" -> new CAP94Seam.BuildManifest(m.sourceCommit(), m.javaVersion(), m.mavenVersion(), m.dependencyManifestSha256(), m.buildConfigurationSha256(), m.environmentFingerprint(), m.buildCommand(), value, m.artifactPath());
            default -> throw new IllegalArgumentException(field);
        };
    }
    private static boolean sameProvenance(CAP94Seam.BuildManifest a, CAP94Seam.BuildManifest b) {
        return Objects.equals(a.sourceCommit(), b.sourceCommit()) && Objects.equals(a.javaVersion(), b.javaVersion())
                && Objects.equals(a.mavenVersion(), b.mavenVersion())
                && Objects.equals(a.dependencyManifestSha256(), b.dependencyManifestSha256())
                && Objects.equals(a.buildConfigurationSha256(), b.buildConfigurationSha256())
                && Objects.equals(a.environmentFingerprint(), b.environmentFingerprint())
                && Objects.equals(a.buildCommand(), b.buildCommand());
    }
    private static boolean manifestComplete(CAP94Seam.BuildManifest m) {
        return present(m.sourceCommit()) && present(m.javaVersion()) && present(m.mavenVersion())
                && sha256(m.dependencyManifestSha256()) && sha256(m.buildConfigurationSha256())
                && present(m.environmentFingerprint()) && present(m.buildCommand()) && present(m.artifactName())
                && m.artifactPath() != null;
    }
    private static boolean validCounts(CAP94Seam.TestReport t) {
        return present(t.command()) && t.total() >= 0 && t.passed() >= 0 && t.failed() >= 0 && t.skipped() >= 0
                && (long) t.total() == (long) t.passed() + t.failed() + t.skipped();
    }
    private static boolean present(String s) { return s != null && !s.isBlank(); }
    private static boolean sha256(String s) { return s != null && s.matches("(?i)[0-9a-f]{64}"); }
    private static String first(String a, String b) { return a != null ? a : b; }
    private static CAP94Seam.ReproducibilityReport result(CAP94Seam.Status status, String lh, String rh,
                                                           String name, boolean countsMatch, String explanation) {
        return new CAP94Seam.ReproducibilityReport(status, lh, rh, name, countsMatch, explanation);
    }
    private static String hash(byte[] raw) throws java.security.GeneralSecurityException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
    }
    private static byte[] stripTimestamp(byte[] raw) {
        return new String(raw, StandardCharsets.UTF_8).replaceAll("mtime=[^\\n]*", "mtime=<normalized>")
                .getBytes(StandardCharsets.UTF_8);
    }
}
