package coverstory.application;

import java.nio.file.Path;

/** Local evidence comparison contract for CAP-94. It never launches a build. */
public final class CAP94Seam {
    public enum Status { REPRODUCIBLE, FAIL, BLOCKED, INVALID_INPUT }

    public record BuildManifest(
            String sourceCommit,
            String javaVersion,
            String mavenVersion,
            String dependencyManifestSha256,
            String buildConfigurationSha256,
            String environmentFingerprint,
            String buildCommand,
            String artifactName,
            Path artifactPath) { }

    public record TestReport(
            String command,
            int total,
            int passed,
            int failed,
            int skipped) { }

    public record BuildEvidence(BuildManifest manifest, TestReport tests) { }

    public record ReproducibilityReport(
            Status status,
            String leftSha256,
            String rightSha256,
            String artifactName,
            boolean testCountsMatch,
            String explanation) { }

    /**
     * Compare two already-produced local evidence bundles. The learner must hash
     * the exact artifact bytes and validate provenance; this method must never
     * normalize archives or execute a build/test command.
     */
    public ReproducibilityReport compareBuilds(BuildEvidence left, BuildEvidence right) {
        // LEARNER TODO: implement the evidence gate. Missing evidence must be
        // BLOCKED (INCOMPLETE), a different source commit INVALID_INPUT, and any
        // byte difference FAIL. Require identical toolchain/configuration and
        // build/test commands plus complete, equal test counts. Hash raw bytes;
        // archive timestamps and other metadata are part of those bytes.
        throw new UnsupportedOperationException("CAP-94 learner TODO: compare supplied build evidence");
    }
}
