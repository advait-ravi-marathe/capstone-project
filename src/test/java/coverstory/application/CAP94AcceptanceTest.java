package coverstory.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Learner-facing acceptance contract; the seam remains intentionally unimplemented. */
@Tag("learnerAcceptance")
class CAP94AcceptanceTest {
    @Test void sameSourceEnvironmentAndBytesRequireMatchingTestCountsAndCommands() {
        var result = new CAP94Seam().compareBuilds(evidence("a.jar"), evidence("b.jar"));
        assertEquals(CAP94Seam.Status.REPRODUCIBLE, result.status());
    }

    @Test void timestampOnlyArchiveByteDifferenceMustFailWithoutNormalization() {
        var result = new CAP94Seam().compareBuilds(evidence("a.jar"), evidence("timestamp-changed.jar"));
        assertEquals(CAP94Seam.Status.FAIL, result.status());
    }

    @Test void differentSourceCommitsAreInvalidInput() {
        var result = new CAP94Seam().compareBuilds(evidence("commit-a.jar"), evidence("commit-b.jar"));
        assertEquals(CAP94Seam.Status.INVALID_INPUT, result.status());
    }

    @Test void absentArtifactOrTestReportIsBlockedAsIncomplete() {
        var result = new CAP94Seam().compareBuilds(evidence("missing.jar"), evidence("no-report.jar"));
        assertEquals(CAP94Seam.Status.BLOCKED, result.status());
    }

    private static CAP94Seam.BuildEvidence evidence(String artifactName) {
        var manifest = new CAP94Seam.BuildManifest("commit-001", "21.0.2", "3.9.9",
                "dep-sha256", "config-sha256", "linux-x86_64", "./mvnw -DskipTests package",
                "coverstory.jar", Path.of("target/cap94", artifactName));
        var report = new CAP94Seam.TestReport("./mvnw test", 42, 42, 0, 0);
        return new CAP94Seam.BuildEvidence(manifest, report);
    }
}
